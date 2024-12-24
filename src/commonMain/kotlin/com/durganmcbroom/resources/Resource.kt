package com.durganmcbroom.resources

public interface Resource {
    public val location: String

    public suspend fun open() : ResourceStream
}