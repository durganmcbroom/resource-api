package com.durganmcbroom.resources

import com.sun.corba.se.impl.ior.ByteBuffer
import kotlinx.coroutines.flow.flow
import java.security.MessageDigest

public actual class VerifiedResource actual constructor(
    public actual val unverifiedResource: Resource,

    private val algorithm: ResourceAlgorithm,
    private val digest: ByteArray,

    private val retryAttempts: Int,
) : Resource {
    override val location: String by unverifiedResource::location

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
                    // Happy path everything is going well
                    if (currentI == i) {
                        i += read.size

                        emit(read)
                    } else if (currentI + read.size > i) { // Overtook where we are supposed to be upon reread
                        // Int cast is safe, this case should never be more than a couple of thousand bytes (if more, your buffers are the wrong size!)
                        val outputBuf = ByteArray((currentI + read.size - i).toInt())
                        val startingIndex = (i - currentI).toInt()

                        System.arraycopy(
                            read,
                            startingIndex,
                            outputBuf,
                            0,
                            outputBuf.size
                        )

                        emit(outputBuf)

                        i += currentI + read.size - i
                    }

                    messageDigest.update(read)
                    currentI += read.size
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

