package com.chand.mobiletina.data

import java.io.IOException

class TgjuClient(private val request: (String) -> String = {
    requestPublicQuote(it, "text/html,application/xhtml+xml;q=0.9,*/*;q=0.8")
}) : UsdRateSource {
    override fun fetchUsd(): FetchedUsdRate {
        val failures = mutableListOf<Exception>()
        for (url in listOf(PUBLIC_USD_PAGE, PUBLIC_CURRENCY_PAGE)) {
            try {
                val rial = TgjuParser.publicHtmlUsdRial(request(url))
                    ?: throw IOException("USD price was not found on TGJU public page")
                val toman = TgjuParser.normalizeToToman(rial)
                if (toman <= 0L) throw IOException("Invalid USD price on TGJU public page")
                return FetchedUsdRate(toman, "TGJU Web")
            } catch (error: Exception) {
                if (Thread.currentThread().isInterrupted) throw error
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
