package com.durganmcbroom.resources

import java.io.Closeable
import java.net.HttpURLConnection
import java.net.URL

public interface CloseableValue<T> : Closeable {
    public val value: T
}

internal val maxConnectionTimeout = System.getProperty("resources.timeout")?.toLong() ?: 20000

public fun <T> URL.useConnection(
    block: (HttpURLConnection) -> T
): CloseableValue<T> = UseTimeout.orNull(maxConnectionTimeout) {
    val httpURLConnection = openConnection() as HttpURLConnection

    object : CloseableValue<T> {
        override val value: T = block(httpURLConnection)

        override fun close() {
            httpURLConnection.disconnect()
        }
    }
} ?: throw ResourceTimedOutException(this@useConnection.toString(), maxConnectionTimeout)

public class ResourceTimedOutException(
    resource: String,
    timeout: Long
) : ResourceException("Error opening connection for the resource: '$resource' because the connection timed out (timeout was: $timeout millis).")