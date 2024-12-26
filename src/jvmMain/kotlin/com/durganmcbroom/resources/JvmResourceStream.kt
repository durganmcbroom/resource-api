package com.durganmcbroom.resources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

public fun InputStream.asResourceStream(): ResourceStream = flow {
    while (true) {
        val buf = ByteArray(max(available(), 1))

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
            val tempBuf = ByteArray(buffer.size + max(DEFAULT_BUFFER_SIZE, size) * 3)
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
