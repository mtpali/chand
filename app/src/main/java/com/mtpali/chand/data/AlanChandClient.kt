package com.mtpali.chand.data

import com.mtpali.chand.BuildConfig
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class AlanChandClient(private val preferences: AppPreferences) {
    data class FetchedRate(val priceToman: Long, val source: String)

    fun fetchUsd(): FetchedRate {
        val token = preferences.effectiveApiToken()
        if (token.isNotBlank()) {
            runCatching { fetchOfficialApi(token) }.getOrNull()?.let { return it }
        }
        return fetchPublicPage()
    }

    private fun fetchOfficialApi(token: String): FetchedRate {
        val json = request(
            url = "${BuildConfig.ALANCHAND_API_URL}?type=currency&symbols=usd",
            bearerToken = token
        )
        val raw = AlanChandParser.officialApiUsdPrice(json)
            ?: error("USD price was not found in AlanChand API response")
        return FetchedRate(AlanChandParser.normalizeToToman(raw), "AlanChand API")
    }

    private fun fetchPublicPage(): FetchedRate {
        val html = request("https://alanchand.com/en/currencies-price")
        val rial = AlanChandParser.publicHtmlUsdSellRial(html)
            ?: error("USD row was not found on AlanChand public page")
        return FetchedRate(rial / 10L, "AlanChand Web")
    }

    private fun request(url: String, bearerToken: String? = null): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/json,text/html;q=0.9,*/*;q=0.8")
            setRequestProperty("Accept-Language", "en-US,en;q=0.8")
            setRequestProperty("User-Agent", "Chand-Android/1.0")
            if (!bearerToken.isNullOrBlank()) {
                setRequestProperty("Authorization", "Bearer $bearerToken")
            }
        }

        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
            if (code !in 200..299) error("HTTP $code: ${body.take(200)}")
            return body
        } finally {
            connection.disconnect()
        }
    }
}
