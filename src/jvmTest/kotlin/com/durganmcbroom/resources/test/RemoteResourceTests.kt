package com.durganmcbroom.resources.test

import com.durganmcbroom.resources.ResourceNotFoundException
import com.durganmcbroom.resources.ResourceOpenException
import com.durganmcbroom.resources.toByteArray
import com.durganmcbroom.resources.toResource
import kotlinx.coroutines.runBlocking
import java.net.URL
import java.util.*
import kotlin.test.Test

class RemoteResourceTests {
    @Test
    fun `Test remote resource downloads correctly`() {
        runBlocking {
            val resource = URL("https://static.extframework.dev/test.txt").toResource()
            val ins = resource.open().toByteArray()

            check(ins.contentEquals(this::class.java.getResourceAsStream("/test.txt")!!.toBytes()))
        }
    }

    @Test
    fun `Test remote resource fails correctly`() {
        runBlocking {
            val resource = runCatching {
                val resource = URL("https://${UUID.randomUUID()}.com").toResource()
                resource.open()
            }
            resource.exceptionOrNull()?.printStackTrace()
            check(resource.exceptionOrNull() is ResourceOpenException)
        }
    }

    @Test
    fun `Test remote resource is not found`() {
        runBlocking {
            val result = runCatching {
                val resource = URL("https://google.com/SomethingRandomThatWillResultInA404").toResource()
                resource.open()
            }
            result.exceptionOrNull()?.printStackTrace()
            check(result.exceptionOrNull() is ResourceNotFoundException)
        }
    }
}