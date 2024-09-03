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
        val resource = URL("https://static.extframework.dev/test.txt").toResource()
        val ins = resource.openStream()

        check(ins.readAllBytes().contentEquals(this::class.java.getResourceAsStream("/test.txt")!!.readAllBytes()))
    }

    @Test
    fun `Test remote resource fails correctly`() {
        val a = runCatching {
            val resource = URL("https://${UUID.randomUUID()}.com").toResource()
            resource.openStream()
        }
        a.exceptionOrNull()?.printStackTrace()
        check(a.exceptionOrNull() is ResourceOpenException)
    }

    @Test
    fun `Test remote resource is not found`() {
        val result = runCatching {
            val resource = URL("https://google.com/SomethingRandomThatWillResultInA404").toResource()
            resource.openStream()
        }
        result.exceptionOrNull()?.printStackTrace()
        check(result.exceptionOrNull() is ResourceNotFoundException)
    }
}