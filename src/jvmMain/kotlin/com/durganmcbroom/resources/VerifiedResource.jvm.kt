package com.durganmcbroom.resources

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest

public actual class VerifiedResource actual constructor(
    public actual val unverifiedResource: Resource,

    private val algorithm: ResourceAlgorithm,
    private val digest: ByteArray,

    private val retryAttempts: Int,
) : Resource {
    override val location: String by unverifiedResource::location

    private inline fun <T> doUntil(attempts: Int, supplier: () -> T?): T? {
        for (i in 0..<attempts) {
            supplier()?.let {
                return it
            }
        }
        return null
    }

    private fun InputStream.readInputStream(): ByteArray = ByteArrayOutputStream().use { buffer ->
        var nRead: Int
        val data = ByteArray(4)

        while (read(data, 0, data.size).also { nRead = it } != -1) {
            buffer.write(data, 0, nRead)
        }

        buffer.flush()
        buffer.toByteArray()
    }

    override fun open(): ResourceStream {
        val messageDigest = MessageDigest.getInstance(
            when (algorithm) {
                ResourceAlgorithm.SHA1 -> "SHA1"
                ResourceAlgorithm.MD5 -> "MD5"
            }
        )

        val bytes = doUntil(retryAttempts) {
            messageDigest.reset()

            val b = DigestInputStream(
                unverifiedResource.openStream(),
                messageDigest
            ).use { it.readInputStream() }

            if (messageDigest.digest().contentEquals(digest)) b
            else null
        } ?: throw ResourceVerificationException(this@VerifiedResource)

        return ByteArrayInputStream(bytes).asResourceStream()
    }
}

