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

fun makeRequest(start:Long, end:Long, range:Boolean): ByteArray?{
    // Make the HTTP requests
    val socket = Socket("127.0.0.1", 8080)
    val os = socket.getOutputStream()
    val ins = socket.getInputStream()
    val request: String = if (range) "GET / HTTP/1.1\r\nHost:127.0.0.1:8080\r\nRange: bytes=$start-$end\r\n\r\n"
        else "GET / HTTP/1.1\r\nHost:127.0.0.1:8080\r\n\r\n"
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
    var end:Long = 0

    // Adjust this value accordingly
    val badReqLimit = 0
    var badReqCount = 0


    // Make first request optimistically
    var reqData: ByteArray? = makeRequest(0, 0, false)
    if (reqData != null) byteArray += reqData

    // Continue with optimistic fetching
    if (byteArray.size < 1024*1024) {
        while(badReqCount < badReqLimit && byteArray.size < 1024*1024) {
            reqData = makeRequest(0, 0, false)
            if (reqData != null) {
                if (reqData.size <= byteArray.size){
                    badReqCount++
                    continue
                } else {
                    val lenBefore = byteArray.size
                    byteArray += reqData.copyOfRange(byteArray.size, reqData.size)
                    // Comment out this line to turn off *consecutive* bad block count
                    badReqCount = 0
                }
            }

        }
    }

    // Fetch the rest of the data in blocks of 64*1024 bytes
    if (byteArray.size < 1024*1024) {
        start = byteArray.size.toLong()
        end = min(start + 64*1024, 1024*1024)
        while(start<=1024*1024){
            val lenBefore = byteArray.size
            reqData = makeRequest(start, end, true)
            if (reqData != null) byteArray += reqData
            // Fetched fewer bytes than block size => we have reached the end of data
            if (byteArray.size - lenBefore < 64*1024) break
            else {
                start += 64*1024
                end += 64*1024
            }
        }
    }



    // SHA-256 hashing
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(byteArray)
    val hex = HexFormat.of().formatHex(digest)

    println("Computed hash is: $hex")
    // Replace the String with the SHA-256 hash value that the server outputted
    println(hex == "c367b764709c4e074c0f5e80e534e63ed4633966cc6956b8e090530f123f7cc2")
}