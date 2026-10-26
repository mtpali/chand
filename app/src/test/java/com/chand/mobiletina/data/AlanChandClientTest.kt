package com.chand.mobiletina.data

import java.io.IOException
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AlanChandClientTest {
    @Test
    fun stopsAtHomepageWhenUsdSaleIsPresent() {
        val calls = mutableListOf<String>()
        val rate = AlanChandClient({ calls += it
            """<tr><td class="currName">دلار آمریکا</td><td class="sellPrice">272,400</td></tr>"""
        }).fetchUsd()
        assertEquals(listOf(AlanChandClient.PUBLIC_HOME), calls)
        assertEquals(FetchedUsdRate(272_400L, "AlanChand"), rate)
    }

    @Test
    fun usesUsdProductPageWhenHomeFails() {
        val calls = mutableListOf<String>()
        val rate = AlanChandClient({ url ->
            calls += url
            if (url == AlanChandClient.PUBLIC_HOME) throw IOException("HTTP 503")
            """<link rel="canonical" href="${AlanChandClient.PUBLIC_USD_PAGE}">
               <script type="application/ld+json">{"@type":"Product","sku":"USD","offers":{
                 "priceCurrency":"IRR","price":"2724000","url":"${AlanChandClient.PUBLIC_USD_PAGE}"}}</script>"""
        }, { Instant.parse("2026-10-26T20:00:00Z").toEpochMilli() }).fetchUsd()
        assertEquals(listOf(AlanChandClient.PUBLIC_HOME, AlanChandClient.PUBLIC_USD_PAGE), calls)
        assertEquals(272_400L, rate.priceToman)
    }

    @Test
    fun failsAfterBothPagesInsteadOfSubstitutingAnotherCurrency() {
        val failure = assertThrows(IOException::class.java) {
            AlanChandClient({ """<tr><td class="currName">تتر</td><td class="sellPrice">270500</td></tr>""" }).fetchUsd()
        }
        assertEquals(2, failure.suppressed.size)
    }
}
