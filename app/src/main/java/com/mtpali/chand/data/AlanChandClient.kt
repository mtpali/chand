package com.mtpali.chand.data

import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class AlanChandClient {
    data class FetchedRate(val priceToman: Long, val source: String)

    fun fetchUsd(): FetchedRate {
        val html = request(PUBLIC_USD_PAGE)
        val rial = AlanChandParser.publicHtmlUsdSellRial(html)
            ?: error("USD row was not found on AlanChand public page")
        return FetchedRate(rial / 10L, "AlanChand Web")
    }

    private fun request(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "text/html,application/xhtml+xml;q=0.9,*/*;q=0.8")
            setRequestProperty("Accept-Language", "en-US,en;q=0.8")
            setRequestProperty("User-Agent", "chand-android")
        }

        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
            if (code !in 200..299) error("HTTP $code")
            return body
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val PUBLIC_USD_PAGE = "https://alanchand.com/en/currencies-price"
    }
}
