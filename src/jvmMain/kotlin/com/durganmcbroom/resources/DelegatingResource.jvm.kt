package com.durganmcbroom.resources

import java.io.InputStream

public fun streamToResource(location: String, delegate: () -> InputStream): Resource = DelegatingResource(location) {
    delegate().asResourceStream()
}