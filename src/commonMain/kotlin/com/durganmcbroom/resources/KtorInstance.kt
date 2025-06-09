package com.durganmcbroom.resources

import io.ktor.client.*
import io.ktor.client.engine.cio.*

public object KtorInstance {
    public val client: HttpClient = HttpClient(CIO) {
        // Enable automatic following of redirects.
        followRedirects = true
    }
}