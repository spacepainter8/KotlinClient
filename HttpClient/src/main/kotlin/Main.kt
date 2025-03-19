package org.example

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headers
import java.io.InputStream
import java.security.MessageDigest
import java.util.Base64
import java.util.HexFormat

suspend fun main() {
    val client = HttpClient(CIO)

    var start: Long = 0
    var end: Long = 64*1024
    var byteArray:ByteArray = ByteArray(0)
//    var data:String= ""


    while(end<=1024*1024){
        val rangeHeader:String = "bytes=$start-$end"
        val response: HttpResponse = client.get("http://127.0.0.1:8080"){
            header(HttpHeaders.Range, rangeHeader)
        }


        if (!response.body<String>().isEmpty()) {
            byteArray += response.bodyAsBytes()
//            data += response.body<String>()
            start += 64*1024
            end += 64*1024
        } else break
    }

    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(byteArray)
    val hex = HexFormat.of().formatHex(digest)
    println(hex == "b48fb3c1d1f0f24741a79f703004f58a1afd04320392e8d0a4d3a5dd9de00a31")

//    print(byteArray.size)
}