package com.durganmcbroom.resources.test

import com.durganmcbroom.resources.ResourceNotFoundException
import com.durganmcbroom.resources.ResourceOpenException
import com.durganmcbroom.resources.openStream
import com.durganmcbroom.resources.toResource
import java.net.URL
import java.util.*
import kotlin.test.Test

class RemoteResourceTests {
    @Test
    fun `Test remote resource downloads correctly`() {
        val resource = URL("http://maven.yakclient.net/snapshots/com/durganmcbroom/resource-api/1.0-SNAPSHOT/maven-metadata.xml.sha1").toResource()
        val ins = resource.openStream()

        check(String(ins.readAllBytes()) == "265cf7d15a69add6d83998098818c9562ceabf33")
    }

    @Test
    fun `Test remote resource fails correctly`() {
        val a = runCatching {
            val resource = URL("http://${UUID.randomUUID()}.com").toResource()
            resource.openStream()
        }
        a.exceptionOrNull()?.printStackTrace()
        check(a.exceptionOrNull() is ResourceOpenException)
    }

    @Test
    fun `Test remote resource is not found`() {
        val result = runCatching {
            val resource = URL("http://google.com/SomethingRandomThatWillResultInA404").toResource()
            resource.openStream()
        }
        result.exceptionOrNull()?.printStackTrace()
        check(result.exceptionOrNull() is ResourceNotFoundException)
    }
}