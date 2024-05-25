package com.durganmcbroom.resources

import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

public class RemoteResource internal constructor(
    private val url: URL,

    private val allowedRedirects: Int,
    private val maxTimeout: Long
) : Resource {
    override val location: String = url.toString()

    private fun openResource(stack: List<URL>): InputStream {
        if (stack.size > allowedRedirects) throw TooManyRedirectsException(
            stack.map(URL::toString),
            allowedRedirects
        )

        return try {
            val closeableValue = stack.last().useConnection(maxTimeout) { conn ->
                when (conn.responseCode) {
                    HttpURLConnection.HTTP_OK -> conn.inputStream
                    HttpURLConnection.HTTP_MOVED_TEMP,
                    HttpURLConnection.HTTP_MOVED_PERM,
                    HttpURLConnection.HTTP_SEE_OTHER -> openResource(
                        stack + URL(conn.getHeaderField("Location"))
                    )

                    else -> throw ResourceNotFoundException(
                        stack.last().toString(),
                        IOException("Received response code '${conn.responseCode}' from the server.")
                    )
                }
            }

            object : InputStream() {
                override fun read(): Int {
                    return closeableValue.value.read()
                }

                override fun close() {
                    closeableValue.close()
                }
            }
        } catch (t: Throwable) {
            throw ResourceOpenException(stack.first().toString(), t)
        }
    }

    override fun open(): ResourceStream {
        return openResource(listOf(url)).asResourceStream()
    }
}

public fun URL.toResource(
    allowedRedirects: Int = 10,
    timeout: Long = 10000
): Resource {
    fun testConnection(stack: List<URL>) {
        val url = stack.last()
        if (stack.size > allowedRedirects) throw TooManyRedirectsException(
            stack.map(URL::toString),
            allowedRedirects
        )

        return try {
            url.useConnection(timeout) { conn ->
                when (conn.responseCode) {
                    HttpURLConnection.HTTP_OK -> { /* Everything is Ok */ }
                    HttpURLConnection.HTTP_MOVED_TEMP,
                    HttpURLConnection.HTTP_MOVED_PERM,
                    HttpURLConnection.HTTP_SEE_OTHER -> {
                        val headerField = conn.getHeaderField("Location")
                        testConnection(
                            stack + URL(headerField)
                        )
                    }

                    else -> throw ResourceNotFoundException(
                        url.toString(),
                        IOException("Received response code '${conn.responseCode}' from the server.")
                    )
                }
            }.close()
        } catch (e: ResourceNotFoundException) {
            throw e
        } catch (t: Throwable) {
            // Its easiest for the user to follow it ths way.
            throw ResourceOpenException(stack.first().toString(), t)
        }
    }

    testConnection(listOf(this@toResource))

    return RemoteResource(this@toResource, allowedRedirects, timeout)
}

public class TooManyRedirectsException(
    redirects: List<String>,
    allowedRedirects: Int
) : ResourceException(
    "In trying to load resource: '${redirects.first()}' had to follow more than $allowedRedirects redirects which was the max amount allotted.\n" +
            "Redirect path followed: '${redirects.joinToString(separator = " -> ")}'"
)

public class ResourceOpenException(
    location: String,
    ex: Throwable
) : ResourceException("Failed to open resource: '${location}' due to the previous error(s).", ex)