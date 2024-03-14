package com.durganmcbroom.resources

import java.io.InputStream
import java.net.URI

public fun Resource(location: String, provider: () -> InputStream) : Resource {
    return object : Resource {
        override val location: String = location
        override fun open(): ResourceStream {
            return provider().asResourceStream()
        }
    }
}

public fun Resource(location: URI, provider: () -> InputStream) : Resource {
    return object : Resource {
        override val location: String = location.toString()
        override fun open(): ResourceStream {
            return provider().asResourceStream()
        }
    }
}

