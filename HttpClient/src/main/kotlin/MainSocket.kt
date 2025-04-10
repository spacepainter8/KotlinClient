package org.example

import okio.internal.commonToUtf8String
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.security.MessageDigest
import java.util.HexFormat
import kotlin.collections.plus
import kotlin.math.min

fun findStartOfData(array: ByteArray): Int{
    // Find the start of data (it starts after the sequence \r\n\r\n)
    var i = 0
    while (i+3 < array.size){
        if (array[i] == '\r'.code.toByte() && array[i+1] == '\n'.code.toByte()
            && array[i+2] == '\r'.code.toByte() && array[i+3] == '\n'.code.toByte()) break
        else i++
    }
    return i+4
}

fun makeRequest(start:Long): ByteArray?{
    // Make the HTTP requests
    val socket = Socket("127.0.0.1", 8080)
    val os = socket.getOutputStream()
    val ins = socket.getInputStream()
    val request: String = "GET / HTTP/1.1\r\nHost:127.0.0.1:8080\r\nRange: bytes=$start-\r\n\r\n"
    os.write(request.toByteArray())
    val response = ins.readBytes()
    val responseString:String = response.decodeToString()
    // First line contains response code
    val firstLine:String = responseString.lines().first()
    os.close()
    ins.close()
    socket.close()
    if (firstLine.contains("200") || firstLine.contains("206")) {
        // Headers and data are separated by \r\n\r\n
        val ans = response.copyOfRange(findStartOfData(response), response.size)
        return ans
    } else return null
}

fun main() {
    var byteArray: ByteArray = ByteArray(0)
    var start:Long = 0

    //
    while(start<=1024*1024){
        var reqData: ByteArray? = makeRequest(start)
        if (reqData == null || reqData.isEmpty()) break
        byteArray += reqData
        start = byteArray.size.toLong()
    }

    // SHA-256 hashing
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(byteArray)
    val hex = HexFormat.of().formatHex(digest)

    println("Computed hash is: $hex")
    // Replace the String with the SHA-256 hash value that the server outputted
    println(hex == "9efe36ff9b2a3fb5f8b9135ce147079eb77a97fca699f2e472126774af178530")
}