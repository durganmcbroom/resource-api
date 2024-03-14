package com.durganmcbroom.resources

public interface ResourceStream : Sequence<Byte> {
    public fun close()
}