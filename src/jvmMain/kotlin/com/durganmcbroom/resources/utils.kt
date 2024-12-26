package com.durganmcbroom.resources

import java.io.InputStream


public fun Resource(location: String, provider: () -> InputStream): Resource {
    return object : Resource {
        override val location: String = location

        override suspend fun open(): ResourceStream =
            provider().asResourceStream()

    }
}