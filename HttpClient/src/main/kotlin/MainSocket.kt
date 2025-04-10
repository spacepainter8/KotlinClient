package org.example

import okio.internal.commonToUtf8String
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.security.MessageDigest
import java.util.HexFormat


fun main() {
    var socket: Socket
    var byteArray: ByteArray = ByteArray(0)
    var start:Long = 0
    var end:Long = 64*1024
    var os: OutputStream
    var ins: InputStream

    var numOfReqs = 0



    while(end<=1024*1024){
        socket = Socket("127.0.0.1", 8080)
        os = socket.getOutputStream()
        ins = socket.getInputStream()

        val request: String = "GET / HTTP/1.1\r\nHost:127.0.0.1:8080\r\nRange: bytes=$start-$end\r\n\r\n"
        os.write(request.toByteArray())
        numOfReqs++
        val response = ins.readBytes()
        val responseString:String = response.decodeToString()
        val firstLine:String = responseString.lines().first()
        val lenBefore = byteArray.size
        if (firstLine.contains("200") || firstLine.contains("206")) {
            val dataLen = responseString.lines()[4].split(" ")[1].toInt()
            val ans = response.copyOfRange(response.size-dataLen, response.size)
            byteArray += ans
        } else println(firstLine)

        if (lenBefore == byteArray.size) break
        else {
            start += 64*1024
            end += 64*1024
        }

        socket.close()
    }


    // SHA-256 hashing
    println(byteArray.size)
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(byteArray)
    val hex = HexFormat.of().formatHex(digest)

    println("Computed hash is: $hex")
    // Replace the String with the SHA-256 hash value that the server outputted
    println(hex == "5e92271591a1b241b1a92d1f17bc940622b58cd7b5b1aef3f21b0f2b4126566d")

    println(numOfReqs)


}