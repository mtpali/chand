package com.chand.mobiletina.data

import java.io.IOException

data class FetchedUsdRate(val priceToman: Long, val source: String)

fun interface UsdRateSource {
    fun fetchUsd(): FetchedUsdRate
}

/** Fixed provider order: Hansha, AlanChand, then TGJU. Stop at the first valid USD quote. */
class DollarClient(
    private val primary: UsdRateSource = HanshaClient(),
    private val secondary: UsdRateSource = AlanChandClient(),
    private val fallback: UsdRateSource = TgjuClient()
) : UsdRateSource {
    override fun fetchUsd(): FetchedUsdRate {
        val failures = mutableListOf<Exception>()
        for (source in listOf(primary, secondary, fallback)) {
            try {
                val rate = source.fetchUsd()
                if (rate.priceToman <= 0L) throw IOException("Invalid USD quote")
                return rate
            } catch (error: Exception) {
                if (Thread.currentThread().isInterrupted) throw error
                failures += error
            }
        }
        // The repository saves only on success, leaving the cached quote intact on failure.
        throw IOException("All USD price sources are unavailable").apply {
            failures.forEach(::addSuppressed)
        }
    }
}
