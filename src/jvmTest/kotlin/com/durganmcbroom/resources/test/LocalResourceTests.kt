package com.durganmcbroom.resources.test

import com.durganmcbroom.resources.ResourceNotFoundException
import com.durganmcbroom.resources.toByteArray
import com.durganmcbroom.resources.toResource
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.toPath
import kotlin.test.Test

class LocalResourceTests {
    @Test
    fun `Test local resource produces correct output`() {
        val url = this::class.java.getResource("/test.txt")!!
        val resource = url.toURI().toPath().toResource()

        runBlocking {
            check(String(resource.open().toByteArray()).startsWith("—— TEST ——"))
        }
    }

    @Test
    fun `Test nonexistent local resources throws correct exception`() {
        val result = runCatching {
            Path("/resource-api-tests/${UUID.randomUUID()}")
                .toResource()
        }

        check(result.exceptionOrNull() is ResourceNotFoundException)
    }
}