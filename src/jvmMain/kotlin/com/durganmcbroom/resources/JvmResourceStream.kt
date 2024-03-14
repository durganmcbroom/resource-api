package com.durganmcbroom.resources

import java.io.BufferedInputStream
import java.io.Closeable
import java.io.InputStream

public class JvmResourceStream(
    public val stream: InputStream,
) : ResourceStream, Closeable {
    override fun close() {
        stream.close()
    }

    override fun iterator(): Iterator<Byte> {
        return BufferedInputStream(stream).iterator()
    }
}

public fun Resource.openStream(): InputStream = open().asInputStream()


public fun ResourceStream.asInputStream(): InputStream = (this as? JvmResourceStream)?.stream!!

public fun InputStream.asResourceStream(): ResourceStream = JvmResourceStream(this)

public class InvalidResourceStreamException() :
    ResourceException("Expected a JvmResourceStream, but found something else.")
