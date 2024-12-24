package com.durganmcbroom.resources

import com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler.BUFFER_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.toList
import sun.security.krb5.Confounder.bytes
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.ArrayList

public fun InputStream.asResourceStream(): ResourceStream = flow {
    val buf = ByteArray(DEFAULT_BUFFER_SIZE)

    while (true) {
        if (read(buf) == -1) break
        emit(buf)
    }

    close()
}.flowOn(Dispatchers.IO)

public suspend fun ResourceStream.toByteArray(): ByteArray {
    var buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var i = 0

    collect {
        val size = it.size
        if (buffer.size >= size + i) {
            System.arraycopy(it, 0, buffer, i, size)
        } else {
            val tempBuf = ByteArray(buffer.size + DEFAULT_BUFFER_SIZE * 3)
            System.arraycopy(buffer, 0, tempBuf, 0, buffer.size)
            System.arraycopy(it, 0, tempBuf, i, size)
            buffer = tempBuf
        }
        i += size
    }

    if (buffer.size == i) {
        return buffer
    }

    val retBuf = ByteArray(i)
    System.arraycopy(buffer, 0, retBuf, 0, i)

    return retBuf
}

//
//public class JvmResourceStream(
//    public val stream: InputStream,
//) : ResourceStream, Closeable {
//    override fun close() {
//        stream.close()
//    }
//
//    override fun iterator(): Iterator<Byte> {
//        return BufferedInputStream(stream).iterator()
//    }
//}
//
//public fun Resource.openStream(): InputStream = open().asInputStream()
//
//
//public fun ResourceStream.asInputStream(): InputStream = (this as? JvmResourceStream)?.stream!!
//
//public fun InputStream.asResourceStream(): ResourceStream = JvmResourceStream(this)
////
//public class InvalidResourceStreamException() :
//    ResourceException("Expected a JvmResourceStream, but found something else.")
