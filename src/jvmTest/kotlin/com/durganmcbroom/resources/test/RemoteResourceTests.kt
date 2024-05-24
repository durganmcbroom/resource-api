package com.durganmcbroom.resources.test

import com.durganmcbroom.resources.ResourceNotFoundException
import com.durganmcbroom.resources.ResourceOpenException
import com.durganmcbroom.resources.openStream
import com.durganmcbroom.resources.toResource
import java.net.URL
import kotlin.test.Test

class RemoteResourceTests {
    @Test
    fun `Test remote resource downloads correctly`() {
        val resource = URL("https://google.com").toResource()
        val ins = resource.openStream()

        check(ins.available() > 100)
        println(String(ins.readAllBytes()))
    }

    @Test
    fun `Test remote resource fails correctly`() {
        val a = runCatching {
            val resource = URL("http://a.com").toResource()
            resource.openStream()
        }
        check(a.exceptionOrNull() is ResourceOpenException)
    }

    @Test
    fun `Test remote resource is not found`() {
        val result = runCatching {
            val resource = URL("http://google.com/SomethingRandomThatWillResultInA404").toResource()
            resource.openStream()
        }
        result.exceptionOrNull()?.printStackTrace()
        check(result.exceptionOrNull() is ResourceOpenException && result.exceptionOrNull()!!.cause is ResourceNotFoundException)
    }
}