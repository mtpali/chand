package com.chand.mobiletina.data

import java.io.IOException

class AlanChandClient(
    private val request: (String) -> String = {
        requestPublicQuote(it, "text/html,application/xhtml+xml;q=0.9,*/*;q=0.8")
    },
    private val nowMillis: () -> Long = System::currentTimeMillis
) : UsdRateSource {
    override fun fetchUsd(): FetchedUsdRate {
        val failures = mutableListOf<Exception>()
        for (url in listOf(PUBLIC_HOME, PUBLIC_USD_PAGE)) {
            try {
                val body = request(url)
                val price = if (url == PUBLIC_HOME) AlanChandParser.homeUsdSellToman(body)
                else AlanChandParser.productUsdSellToman(body, nowMillis())
                if (price == null || price <= 0L) throw IOException("No valid USD sale quote on AlanChand")
                return FetchedUsdRate(price, "AlanChand")
            } catch (error: Exception) {
                if (Thread.currentThread().isInterrupted) throw error
                failures += error
            }
        }
        throw IOException("Unable to fetch USD price from AlanChand").apply {
            failures.forEach(::addSuppressed)
        }
    }

    companion object {
        val PUBLIC_HOME: String get() = SourceEndpoints.alanHome
        val PUBLIC_USD_PAGE: String get() = SourceEndpoints.alanUsd
    }
}
