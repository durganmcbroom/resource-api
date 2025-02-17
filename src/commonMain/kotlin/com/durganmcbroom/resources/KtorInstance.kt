package com.durganmcbroom.resources

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*

public object KtorInstance {
    public val client: HttpClient = HttpClient(CIO) {
        engine {
            maxConnectionsCount = 100
            endpoint {
                connectTimeout = 60_000
                socketTimeout = 60_000
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 60_000
            socketTimeoutMillis = 60_000
        }

        // Enable automatic following of redirects.
        followRedirects = true
    }
}