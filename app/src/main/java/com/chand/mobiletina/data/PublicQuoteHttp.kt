package com.chand.mobiletina.data

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Small public GET requests only. No recovered keys or bearer tokens are needed. */
internal fun requestPublicQuote(url: String, accept: String): String {
    val endpoint = URL(url)
    require(endpoint.protocol == "https")
    val connection = (endpoint.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 8_000
        readTimeout = 10_000
        instanceFollowRedirects = true
        useCaches = false
        setRequestProperty("Accept", accept)
        setRequestProperty("Accept-Language", "fa-IR,fa;q=0.9,en;q=0.7")
        setRequestProperty("Cache-Control", "no-cache")
        setRequestProperty("User-Agent", "chand-android")
    }
    try {
        val code = connection.responseCode
        if (code !in 200..299) throw IOException("HTTP $code from price source")
        val bytes = connection.inputStream.use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8_192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (output.size() + count > MAX_RESPONSE_BYTES) {
                    throw IOException("Price response exceeds size limit")
                }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
        return bytes.toString(Charsets.UTF_8)
    } finally {
        connection.disconnect()
    }
}

private const val MAX_RESPONSE_BYTES = 2 * 1_024 * 1_024
