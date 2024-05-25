package com.durganmcbroom.resources.test

import com.durganmcbroom.resources.*
import java.net.URL
import java.util.*
import kotlin.test.Test

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
            verifiedResource.openStream()
        }
        r.exceptionOrNull()?.printStackTrace()
        check(r.exceptionOrNull() is ResourceVerificationException)
    }
}