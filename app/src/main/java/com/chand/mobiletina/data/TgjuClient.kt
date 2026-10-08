package com.chand.mobiletina.data

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class TgjuClient(private val request: (String) -> String = ::requestPublicPage) {
    data class FetchedRate(val priceToman: Long, val source: String)

    fun fetchUsd(): FetchedRate {
        val failures = mutableListOf<Exception>()
        for (url in listOf(PUBLIC_USD_PAGE, PUBLIC_CURRENCY_PAGE)) {
            try {
                val rial = TgjuParser.publicHtmlUsdRial(request(url))
                    ?: throw IOException("USD price was not found on TGJU public page")
                val toman = TgjuParser.normalizeToToman(rial)
                if (toman <= 0L) throw IOException("Invalid USD price on TGJU public page")
                return FetchedRate(toman, "TGJU Web")
            } catch (error: Exception) {
                failures += error
            }
        }
        throw IOException("Unable to fetch USD price from TGJU").apply {
            failures.forEach(::addSuppressed)
        }
    }

    companion object {
        const val PUBLIC_USD_PAGE = "https://www.tgju.org/profile/price_dollar_rl"
        const val PUBLIC_CURRENCY_PAGE = "https://www.tgju.org/currency"
    }
}

private fun requestPublicPage(url: String): String {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 10_000
        readTimeout = 10_000
        instanceFollowRedirects = true
        useCaches = false
        setRequestProperty("Accept", "text/html,application/xhtml+xml;q=0.9,*/*;q=0.8")
        setRequestProperty("Accept-Language", "fa-IR,fa;q=0.9,en;q=0.7")
        setRequestProperty("Cache-Control", "no-cache")
        setRequestProperty("User-Agent", "chand-android")
    }

    try {
        val code = connection.responseCode
        if (code !in 200..299) throw IOException("HTTP $code from TGJU")
        return connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    } finally {
        connection.disconnect()
    }
}
