package com.durganmcbroom.resources.test

import com.durganmcbroom.resources.*
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.net.URL
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class VerifiedResourceTests {
    @Test
    fun `Test remote resource verification works as intended`() {
        val remoteResource =
            URL("https://repo1.maven.org/maven2/commons-io/commons-io/2.9.0/commons-io-2.9.0.jar").toResource()

        val verifiedResource = VerifiedResource(
            remoteResource,
            ResourceAlgorithm.SHA1,
            HexFormat.of().parseHex("9cb4ce5672f6db9eb0ef6b44910e1ed96fc50e5e")
        )
        val r = verifiedResource.openStream()

        check(r.readAllBytes().size == 325259)
    }

    @Test
    fun `Invalid verification works correctly`() {
        val remoteResource =
            URL("https://repo1.maven.org/maven2/commons-io/commons-io/2.9.0/commons-io-2.9.0.jar").toResource()

        val verifiedResource = VerifiedResource(
            remoteResource,
            ResourceAlgorithm.SHA1,
            HexFormat.of().parseHex("")
        )

        val r = runCatching {
            verifiedResource.openStream().readAllBytes()
        }
        r.exceptionOrNull()?.printStackTrace()
        check(r.exceptionOrNull() is ResourceVerificationException)
    }

    @Test
    fun `Test small resource reads correctly`() {
        val rawResource = Resource("test") { this::class.java.getResourceAsStream("/local-test.txt")!! }

        val verifiedResource = VerifiedResource(
            rawResource, ResourceAlgorithm.SHA1,
            MessageDigest.getInstance("SHA1").digest(rawResource.openStream().readAllBytes()),
            5
        )

        assertEquals(String(verifiedResource.openStream().readAllBytes()), "This is local.")
    }

    @Test
    fun `Test small resource fails correctly`() {
        val rawResource = Resource("test") { ByteArrayInputStream(byteArrayOf(84, 104, 105, 115, 32)) }

        val verifiedResource = VerifiedResource(
            rawResource, ResourceAlgorithm.SHA1,
            MessageDigest.getInstance("SHA1").digest(byteArrayOf(1)),
            5
        )

        assertThrows<ResourceVerificationException> {
            verifiedResource.openStream().readAllBytes()
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

            override fun open(): ResourceStream {
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

        assertEquals(String(verifiedResource.open().asInputStream().readAllBytes()), correct)
    }
}