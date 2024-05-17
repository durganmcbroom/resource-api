package com.durganmcbroom.resources

import java.net.HttpURLConnection
import java.net.URL

public inline fun <T> URL.useConnection(
    timeout: Long = 10000,
    crossinline block: (HttpURLConnection) -> T
): T  {
    return UseTimeout.orNull(timeout) {
        block(openConnection() as HttpURLConnection)
    } ?: throw ResourceTimedOutException(this@useConnection.toString(), timeout)
}

public class ResourceTimedOutException(
    resource: String,
    timeout: Long
) : ResourceException("Error opening connection for the resource: '$resource' because the connection timed out (timeout was: $timeout millis).")