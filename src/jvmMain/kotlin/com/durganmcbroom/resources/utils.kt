package com.durganmcbroom.resources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URI



public fun Resource(location: String, provider: () -> InputStream): Resource {
    return object : Resource {
        override val location: String = location

        override suspend fun open(): ResourceStream =
            provider().asResourceStream()

    }
}