package com.durganmcbroom.resources

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpTimeout

public object KtorInstance {
    public val client: HttpClient = HttpClient(CIO) {
        // Enable automatic following of redirects.
        followRedirects = true

        engine {
            requestTimeout = 0
        }
        install(HttpTimeout) {
            this.socketTimeoutMillis = Long.MAX_VALUE
            this.connectTimeoutMillis = Long.MAX_VALUE
            this.requestTimeoutMillis = Long.MAX_VALUE
        }
    }
}