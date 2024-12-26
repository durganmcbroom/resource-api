package com.durganmcbroom.resources

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.*

public object KtorInstance {
    public val client: HttpClient = HttpClient(Apache) {
        install(HttpTimeout) {
            requestTimeoutMillis = 20000
        }
        engine {
            pipelining = true
            followRedirects = true
        }
    }
}