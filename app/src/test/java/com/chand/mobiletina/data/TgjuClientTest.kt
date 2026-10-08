package com.chand.mobiletina.data

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TgjuClientTest {
    @Test
    fun usesTheProfileWithoutRequestingTheFallbackWhenItSucceeds() {
        val requests = mutableListOf<String>()
        val rate = TgjuClient { url ->
            requests += url
            """<link rel="canonical" href="${TgjuClient.PUBLIC_USD_PAGE}">
                <span data-col="info.last_trade.PDrCotVal">2,668,050</span>"""
        }.fetchUsd()
        assertEquals(listOf(TgjuClient.PUBLIC_USD_PAGE), requests)
        assertEquals(266_805L, rate.priceToman)
        assertEquals("TGJU Web", rate.source)
    }

    @Test
    fun fallsBackToTheCurrencyTableAfterANetworkFailure() {
        val requests = mutableListOf<String>()
        val rate = TgjuClient { url ->
            requests += url
            if (url == TgjuClient.PUBLIC_USD_PAGE) throw IOException("HTTP 503")
            table
        }.fetchUsd()
        assertEquals(listOf(TgjuClient.PUBLIC_USD_PAGE, TgjuClient.PUBLIC_CURRENCY_PAGE), requests)
        assertEquals(266_200L, rate.priceToman)
    }

    @Test
    fun fallsBackWhenTheProfileHasNoValidPrice() {
        val rate = TgjuClient { url ->
            if (url == TgjuClient.PUBLIC_USD_PAGE) "<h1>دلار</h1><p>—</p>" else table
        }.fetchUsd()
        assertEquals(266_200L, rate.priceToman)
    }

    @Test
    fun failsWhenBothPagesFailInsteadOfReturningZeroOrAnotherCurrency() {
        val error = assertThrows(IOException::class.java) {
            TgjuClient { "<table><tr data-market-row='price_eur' data-price='2993300'></tr></table>" }
                .fetchUsd()
        }
        assertEquals(2, error.suppressed.size)
    }

    private val table = """
        <table><tr data-market-row="price_dollar_rl" data-price="2,662,000"></tr></table>
    """.trimIndent()
}
