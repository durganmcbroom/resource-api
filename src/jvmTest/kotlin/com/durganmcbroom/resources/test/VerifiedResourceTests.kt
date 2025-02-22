package com.durganmcbroom.resources.test

import com.durganmcbroom.resources.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.net.URL
import java.security.MessageDigest
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class VerifiedResourceTests {
    @Test
    fun `Test remote resource verification works as intended`() {
        runBlocking {
            val remoteResource =
                URL("https://repo1.maven.org/maven2/commons-io/commons-io/2.9.0/commons-io-2.9.0.jar").toResource()

            val verifiedResource = VerifiedResource(
                remoteResource,
                ResourceAlgorithm.SHA1,
                byteArrayOf(
                    -100,
                    -76,
                    -50,
                    86,
                    114,
                    -10,
                    -37,
                    -98,
                    -80,
                    -17,
                    107,
                    68,
                    -111,
                    14,
                    30,
                    -39,
                    111,
                    -59,
                    14,
                    94
                ),
            )
            val r = verifiedResource.open()

            check(r.toByteArray().size == 325259)
        }
    }

    @Test
    fun `Invalid verification works correctly`() {
        runBlocking {
            val remoteResource =
                URL("https://repo1.maven.org/maven2/commons-io/commons-io/2.9.0/commons-io-2.9.0.jar").toResource()

            val verifiedResource = VerifiedResource(
                remoteResource,
                ResourceAlgorithm.SHA1,
                byteArrayOf()
            )

            val r = runCatching {
                verifiedResource.open().toByteArray()
            }

            r.exceptionOrNull()?.printStackTrace()
            check(r.exceptionOrNull() is ResourceVerificationException)
        }
    }

    @Test
    fun `Test small resource reads correctly`() {
        runBlocking {
            val rawResource = Resource("test") { this::class.java.getResourceAsStream("/local-test.txt")!! }

            val verifiedResource = VerifiedResource(
                rawResource, ResourceAlgorithm.SHA1,
                MessageDigest.getInstance("SHA1").digest(rawResource.open().toByteArray()),
                5
            )

            assertEquals(String(verifiedResource.open().toByteArray()), "This is local.")
        }
    }

    @Test
    fun `Test small resource fails correctly`() {
        runBlocking {
            val rawResource = Resource("test") { ByteArrayInputStream(byteArrayOf(84, 104, 105, 115, 32)) }

            val verifiedResource = VerifiedResource(
                rawResource, ResourceAlgorithm.SHA1,
                MessageDigest.getInstance("SHA1").digest(byteArrayOf(1)),
                5
            )

            assertThrows<ResourceVerificationException> {
                runBlocking {
                    verifiedResource.open().toByteArray()
                }
            }
        }
    }

    @Test
    fun `Test cut off resource completes after failure`() {
        val correct = "Hello this is a test"
        val rawResource = object : Resource {
            override val location: String = "test"

            private var opens = 0

            val wrong = Resource("test") { ByteArrayInputStream("Hello this ".toByteArray()) }
            val right = Resource("test") { ByteArrayInputStream(correct.toByteArray()) }

            override suspend fun open(): ResourceStream {
                val r = if (opens == 0) wrong.open()
                else right.open()

                opens++

                return r
            }
        }

        val verifiedResource = VerifiedResource(
            rawResource, ResourceAlgorithm.SHA1,
            MessageDigest.getInstance("SHA1").digest(correct.toByteArray()),
            5
        )

        runBlocking {
            assertEquals(String(verifiedResource.open().toByteArray()), correct)
        }
    }
}