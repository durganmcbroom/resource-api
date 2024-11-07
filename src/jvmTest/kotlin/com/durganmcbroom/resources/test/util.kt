package com.durganmcbroom.resources.test

import java.io.ByteArrayOutputStream
import java.io.InputStream

fun InputStream.toBytes(): ByteArray {
    val buffer = ByteArrayOutputStream()
    val data = ByteArray(1024)
    var bytesRead: Int
    while (this.read(data).also { bytesRead = it } != -1) {
        buffer.write(data, 0, bytesRead)
    }
    return buffer.toByteArray()
}