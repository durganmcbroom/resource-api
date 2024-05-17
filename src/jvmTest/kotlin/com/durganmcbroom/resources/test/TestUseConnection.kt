package com.durganmcbroom.resources.test

import com.durganmcbroom.resources.ResourceTimedOutException
import com.durganmcbroom.resources.UseTimeout
import com.durganmcbroom.resources.useConnection
import java.net.URL
import kotlin.test.Test

class TestUseConnection {
    @Test
    fun `Test UseTimeout`() {
        val timeout = 1000L

        val startTime = System.currentTimeMillis()
        UseTimeout.orNull(timeout) {
            Thread.sleep(10000)
        }

        println(System.currentTimeMillis() - startTime)
        check(System.currentTimeMillis() - startTime <= timeout + 10) // Very dependent on system timings, should always be within 10ms though
    }


    @Test
    fun `Test UseTimeout returns correctly`() {
        val str = UseTimeout.orNull(1000L) {
            Thread.sleep(500)
            "yay!"
        }

        check(str == "yay!")
    }

    @Test
    fun `Test use connection connects correctly`() {
        URL("https://google.com").useConnection {
            val readAllBytes = it.inputStream.readAllBytes()
            check(readAllBytes.size > 1000)
            println(String(readAllBytes))
        }
    }
}