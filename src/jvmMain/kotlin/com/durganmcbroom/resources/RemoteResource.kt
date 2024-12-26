package com.durganmcbroom.resources

import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.prepareGet
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.flow.flow
import kotlinx.io.readByteArray
import java.io.IOException
import java.net.URL

public class RemoteResource(
    public val request: HttpRequestBuilder,
) : Resource {
    override val location: String = request.url.toString()

    override suspend fun open(): ResourceStream = flow {
        try {
            KtorInstance.client.prepareGet(request).execute { httpResponse ->
                if (httpResponse.status == HttpStatusCode.NotFound) {
                    throw ResourceNotFoundException(location)
                }
                if (!httpResponse.status.isSuccess()) {
                    throw ResourceOpenException(location, Exception("Status: '${httpResponse.status}' received."))
                }

                val channel: ByteReadChannel = httpResponse.body()
                while (!channel.isClosedForRead) {
                    val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                    while (!packet.exhausted()) {
                        val bytes = packet.readByteArray()
                        emit(bytes)
                    }
                }
            }
        } catch (ex: Exception) {
            if (ex !is ResourceException) {
                throw ResourceOpenException(location, ex)
            }
            throw ex
        }
    }
}

public suspend fun HttpRequestBuilder.toResource(): Resource {
    val client by KtorInstance::client

    val testConn = try {
        client.request(this)
    } catch (ex: Exception) {
        throw ResourceOpenException(url.toString(), ex)
    }

    if (testConn.status.value !in 200..299) throw ResourceNotFoundException(
        url.toString(),
        IOException("Received response code '${testConn.status}' from the server.")
    )

    return RemoteResource(this)
}

public suspend fun URL.toResource(): Resource {
    val builder = HttpRequestBuilder().apply {
        url(this@toResource)
    }

    return builder.toResource()
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