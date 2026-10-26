package com.chand.mobiletina.data

import java.io.IOException

class HanshaClient(
    private val request: (String) -> String = { requestPublicQuote(it, "application/json") },
    private val nowMillis: () -> Long = System::currentTimeMillis
) : UsdRateSource {
    override fun fetchUsd(): FetchedUsdRate {
        val price = HanshaParser.usdSellToman(request(PUBLIC_RATES_URL), nowMillis())
            ?: throw IOException("No valid, recent USD quote in Hansha response")
        return FetchedUsdRate(price, "Hansha")
    }

    companion object {
        val PUBLIC_RATES_URL: String get() = SourceEndpoints.hanshaRates
    }
}
