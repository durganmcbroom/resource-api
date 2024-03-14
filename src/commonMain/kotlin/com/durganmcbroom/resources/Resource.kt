package com.durganmcbroom.resources


public interface Resource {
    public val location: String

    @Throws(ResourceException::class)
    public fun open() : ResourceStream
}