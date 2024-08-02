package com.durganmcbroom.resources

import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import kotlin.math.min

public actual class VerifiedResource actual constructor(
    public actual val unverifiedResource: Resource,

    private val algorithm: ResourceAlgorithm,
    private val digest: ByteArray,

    private val retryAttempts: Int,
) : Resource {
    override val location: String by unverifiedResource::location

    private fun InputStream.forceSkip(n: Long): Long {
        var remaining: Long = n
        var nr: Int

        if (n <= 0) {
            return 0
        }

        val size =
            min(2048.0, remaining.toDouble()).toInt()
        val skipBuffer = ByteArray(size)
        while (remaining > 0) {
            nr = read(skipBuffer, 0, min(size.toDouble(), remaining.toDouble()).toInt())
            if (nr < 0) {
                break
            }
            remaining -= nr.toLong()
        }

        return n - remaining
    }

    override fun open(): ResourceStream {
        val messageDigest = MessageDigest.getInstance(
            when (algorithm) {
                ResourceAlgorithm.SHA1 -> "SHA1"
                ResourceAlgorithm.MD5 -> "MD5"
            }
        )

        return object : InputStream() {
            private var i = 0L

            private var attempts = 0
            private var delegate = DigestInputStream(
                unverifiedResource.openStream(),
                messageDigest
            )

            override fun read(): Int {
                var read = delegate.read()

                if (read == -1 && !messageDigest.digest().contentEquals(digest)) {
                    if (attempts >= retryAttempts) throw ResourceVerificationException(this@VerifiedResource)

                    messageDigest.reset()
                    delegate = DigestInputStream(
                        unverifiedResource.openStream(),
                        messageDigest
                    )

                    attempts++
                    delegate.forceSkip(i)
                    read = read()
                }

                i++
                return read
            }

            override fun close() {
                delegate.close()
            }

            override fun available(): Int {
                return delegate.available()
            }
        }.asResourceStream()
    }
}

