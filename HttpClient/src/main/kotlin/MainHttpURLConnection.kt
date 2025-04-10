package org.example

import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.security.MessageDigest
import java.util.HexFormat


fun main() {

    val url: URL = URI.create("http://127.0.0.1:8080").toURL()

    // Response storage
    var byteArray:ByteArray = ByteArray(0)
    // Defines the chunk of data that's being received, 64*1024 is based on server's code, this way we assure
    //  data isn't randomized
    var start:Long = 0
    var end:Long = 64*1024


    while(end<=1024*1024){
        val conn:HttpURLConnection = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        // Range header for only receiving a chunk of data
        val rangeHeader:String = "bytes=$start-$end"
        conn.setRequestProperty("Range", rangeHeader)
        val responseCode = conn.responseCode
        val lenBefore = byteArray.size
        if (responseCode == 200 || responseCode == 206) {
            conn.inputStream.use {
                byteArray += it.readBytes()
            }
        } else println(responseCode)

        // this condition checks whether there was new data added
        if (lenBefore == byteArray.size) break
        else {
            start += 64*1024
            end += 64*1024
        }
        conn.disconnect()
    }

    // SHA-256 hashing
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(byteArray)
    val hex = HexFormat.of().formatHex(digest)

    println("Computed hash is: $hex")
    // Replace the String with the SHA-256 hash value that the server outputted
    println(hex == "034597fb047a9160d30bde559ea0962a68b5a904b5fa7e5335f8eeb01f453849")


}