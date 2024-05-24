package com.durganmcbroom.resources.test

import com.durganmcbroom.resources.*
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
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
       val a= runCatching {
            val resource = URL("http://a.com").toResource()
            resource.openStream()
        }
        check(a.exceptionOrNull() is ResourceOpenException)
    }

//    private fun openResource(stack: List<URL>): InputStream {
//        if (stack.size > 10) throw TooManyRedirectsException(
//            stack.map(URL::toString),
//            10
//        )
//
//        return try {
//            stack.last().useConnection(100000) { conn ->
//                when (conn.responseCode) {
//                    HttpURLConnection.HTTP_OK -> conn.inputStream
//                    HttpURLConnection.HTTP_MOVED_TEMP,
//                    HttpURLConnection.HTTP_MOVED_PERM,
//                    HttpURLConnection.HTTP_SEE_OTHER -> openResource(
//                        stack + URL(conn.getHeaderField("Location"))
//                    )
//                    else -> throw ResourceNotFoundException(
//                        stack.last().toString(),
//                        IOException("Received response code '${conn.responseCode}' from the server.")
//                    )
//                }
//            }
//        } catch (t: Throwable) {
//            throw ResourceOpenException(stack.first().toString(), t)
//        }
//    }

    @Test
    fun `resource open`() {
        val url  = URL("http://maven.yakclient.net/snapshots/net/yakclient/client/1.1-SNAPSHOT/client-1.1-20240522.010324-6-all.jar")

        val openStream = url.toResource(timeout = 30000).openStream() //RemoteResource(url, 10).openStream()

        println(openStream.available())
        println((String(openStream.readAllBytes())))
    }

//    @Test
//    fun `raw open`() {
//        val url  = URL("http://maven.yakclient.net/snapshots/net/yakclient/client/1.1-SNAPSHOT/client-1.1-20240522.010324-6-all.jar")
//
//        fun testConnection(stack: List<URL>) {
//            val url = stack.last()
//            if (stack.size > 10) throw TooManyRedirectsException(
//                stack.map(URL::toString),
//                10
//            )
//
//            return try {
//                url.useConnection { conn ->
//                    when (conn.responseCode) {
//                        HttpURLConnection.HTTP_OK -> { /* Everything is Ok */
//                        }
//
//                        HttpURLConnection.HTTP_MOVED_TEMP,
//                        HttpURLConnection.HTTP_MOVED_PERM,
//                        HttpURLConnection.HTTP_SEE_OTHER -> {
//                            val headerField = conn.getHeaderField("Location")
//                            testConnection(
//                                stack + URL(headerField)
//                            )
//                        }
//
//                        else -> throw ResourceNotFoundException(
//                            url.toString(),
//                            IOException("Received response code '${conn.responseCode}' from the server.")
//                        )
//                    }
//                    conn.disconnect()
//                }
//            } catch (t: Throwable) {
//                // Its easiest for the user to follow it ths way.
//                throw ResourceOpenException(stack.first().toString(), t)
//            }
//        }
//
//        testConnection(listOf(url))
//        val inputStream = openResource(listOf(url)).asResourceStream().asInputStream()
//
//        println(inputStream.available())
//        println((String(inputStream.readAllBytes())))
//    }
}