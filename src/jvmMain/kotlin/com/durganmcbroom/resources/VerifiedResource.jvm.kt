package com.durganmcbroom.resources

import kotlinx.coroutines.flow.flow
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

    override suspend fun open(): ResourceStream {
        return flow {
            var i = 0L

            var attempts = 0

            val messageDigest = MessageDigest.getInstance(
                when (algorithm) {
                    ResourceAlgorithm.SHA1 -> "SHA1"
                    ResourceAlgorithm.MD5 -> "MD5"
                }
            )
            messageDigest.reset()

            while (attempts < retryAttempts) {
                val delegate = unverifiedResource.open()

                var currentI = 0L
                delegate.collect { read ->
                    if (currentI == i) {
                        i++

                        emit(read)
                    }

                    messageDigest.update(read)
                    currentI++
                }

                if (!messageDigest.digest().contentEquals(digest)) {
                    messageDigest.reset()

                    attempts++
                } else {
                    return@flow
                }
            }

            throw ResourceVerificationException(this@VerifiedResource)
        }
    }
}

