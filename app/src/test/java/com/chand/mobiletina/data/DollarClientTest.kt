package com.chand.mobiletina.data

import java.io.IOException
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DollarClientTest {
    private val now = Instant.parse("2026-10-26T20:00:00Z").toEpochMilli()
    private val profile = """<link rel="canonical" href="${TgjuClient.PUBLIC_USD_PAGE}">
        <span data-col="info.last_trade.PDrCotVal">2,668,050</span>"""

    @Test
    fun requestsPublicHanshaFirstAndNeverTouchesFallbackWhenValid() {
        val requests = mutableListOf<String>()
        val primary = HanshaClient({ url ->
            requests += url
            """[{"ab":"usd","ps":[{"sp":270500,"ts":"2026-10-26T19:30:00Z"}]}]"""
        }, { now })
        val fallback = TgjuClient { throw AssertionError("Fallback should not be requested") }
        assertEquals(FetchedUsdRate(270_500L, "Hansha"), DollarClient(primary, fallback).fetchUsd())
        assertEquals(listOf(HanshaClient.PUBLIC_RATES_URL), requests)
    }

    @Test
    fun usesExistingTgjuProfileAfterHanshaNetworkFailure() {
        val requests = mutableListOf<String>()
        val primary = HanshaClient({ requests += it; throw IOException("HTTP 503") }, { now })
        val fallback = TgjuClient { requests += it; profile }
        val rate = DollarClient(primary, fallback).fetchUsd()
        assertEquals(listOf(HanshaClient.PUBLIC_RATES_URL, TgjuClient.PUBLIC_USD_PAGE), requests)
        assertEquals(FetchedUsdRate(266_805L, "TGJU Web"), rate)
    }

    @Test
    fun malformedOrMissingUsdFallsBackThroughBothExistingTgjuRoutes() {
        for (payload in listOf("bad JSON", "[]", """[{"ab":"usdt","ps":[]}]""")) {
            val requests = mutableListOf<String>()
            val primary = HanshaClient({ requests += it; payload }, { now })
            val fallback = TgjuClient { url ->
                requests += url
                if (url == TgjuClient.PUBLIC_USD_PAGE) "<h1>Unavailable</h1>"
                else """<tr data-market-row="price_dollar_rl" data-price="2,662,000"></tr>"""
            }
            val rate = DollarClient(primary, fallback).fetchUsd()
            assertEquals(listOf(HanshaClient.PUBLIC_RATES_URL, TgjuClient.PUBLIC_USD_PAGE,
                TgjuClient.PUBLIC_CURRENCY_PAGE), requests)
            assertEquals(266_200L, rate.priceToman)
        }
    }

    @Test
    fun zeroPrimaryQuoteCannotReplaceCachedPrice() {
        val rate = DollarClient(
            UsdRateSource { FetchedUsdRate(0, "invalid") },
            UsdRateSource { FetchedUsdRate(266_200, "fallback") }
        ).fetchUsd()
        assertEquals(266_200L, rate.priceToman)
    }

    @Test
    fun totalFailureIsAnErrorRatherThanAZeroQuote() {
        val failure = assertThrows(IOException::class.java) {
            DollarClient(
                UsdRateSource { throw IOException("primary down") },
                UsdRateSource { throw IOException("fallback down") }
            ).fetchUsd()
        }
        assertEquals(2, failure.suppressed.size)
    }

    @Test
    fun invalidFallbackAlsoFailsInsteadOfSavingZero() {
        assertThrows(IOException::class.java) {
            DollarClient(
                UsdRateSource { throw IOException("primary down") },
                UsdRateSource { FetchedUsdRate(-1, "invalid") }
            ).fetchUsd()
        }
    }
}
