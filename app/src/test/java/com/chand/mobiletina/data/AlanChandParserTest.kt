package com.chand.mobiletina.data

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AlanChandParserTest {
    private val now = Instant.parse("2026-10-26T20:00:00Z").toEpochMilli()

    @Test
    fun selectsOnlyUsdSaleCellAndKeepsHomepageTomanUnit() {
        val html = row("دلار استانبول", "999,999") + row("تتر", "888,888") + row("دلار آمریکا", "۲۷۲,۴۰۰")
        assertEquals(272_400L, AlanChandParser.homeUsdSellToman(html))
    }

    @Test
    fun supportsArabicDigitsEntitiesAndNestedPriceSpans() {
        assertEquals(272_400L, AlanChandParser.homeUsdSellToman(row("دلار آمریکا", "٢٧٢٬٤٠٠")))
        assertEquals(272_400L, AlanChandParser.homeUsdSellToman(row("US Dollar", "<b>272&#44;400</b>")))
    }

    @Test
    fun ignoresPricesInScriptsCommentsBuyAndConversionCells() {
        val hidden = """<script>const example = '${row("دلار آمریکا", "999,999")}';</script>
            <!--${row("دلار آمریکا", "888,888")}-->"""
        assertNull(AlanChandParser.homeUsdSellToman(hidden))
        assertNull(AlanChandParser.homeUsdSellToman(row("دلار آمریکا", "—")))
        assertEquals(272_400L, AlanChandParser.homeUsdSellToman(hidden + row("دلار آمریکا", "272,400")))
    }

    @Test
    fun rejectsEmptyZeroNegativeFractionalAndOverflowPrices() {
        for (price in listOf("", "—", "0", "-272400", "272400.5", "9223372036854775808", "272400 (0.44%)")) {
            assertNull(price, AlanChandParser.homeUsdSellToman(row("دلار آمریکا", price)))
        }
    }

    @Test
    fun convertsOnlyExplicitProductRialAndSupportsTomanUnit() {
        assertEquals(272_400L, AlanChandParser.productUsdSellToman(product("2724000", "IRR"), now))
        assertEquals(272_400L, AlanChandParser.productUsdSellToman(product("272400", "IRT"), now))
        assertNull(AlanChandParser.productUsdSellToman(product("272400", "USD"), now))
    }

    @Test
    fun rejectsOtherCurrencyProductsCanonicalPagesAndFakeScripts() {
        assertNull(AlanChandParser.productUsdSellToman(product("2724000", "IRR").replace("\"USD\"", "\"USDT\""), now))
        assertNull(AlanChandParser.productUsdSellToman(product("2724000", "IRR").replace("currencies-price/usd", "currencies-price/eur"), now))
        assertNull(AlanChandParser.productUsdSellToman(product("2724000", "IRR").replace("application/ld+json", "text/javascript"), now))
        assertNull(AlanChandParser.productUsdSellToman("<!--${product("2724000", "IRR")}-->", now))
    }

    @Test
    fun rejectsExpiredMalformedAndNonpositiveProductOffers() {
        for (price in listOf("0", "-1", "bad", "2724000.5", "9223372036854775808")) {
            assertNull(price, AlanChandParser.productUsdSellToman(product(price, "IRR"), now))
        }
        assertNull(AlanChandParser.productUsdSellToman(product("2724000", "IRR")
            .replace("2026-10-27T00:15:07+03:30", "2026-10-25T00:15:07+03:30"), now))
        assertNull(AlanChandParser.productUsdSellToman("<script type='application/ld+json'>{</script>", now))
    }

    private fun row(name: String, sell: String): String = """
        <tr onclick="window.location='https://alanchand.com/currencies-price/usd'">
          <td class="currName"><span class="flag-container"></span>$name</td>
          <td class="buyPrice text-center">269,700</td>
          <td class="sellPrice text-center">$sell<span class="priceSymbol up"></span></td>
          <td class="usdRate text-center">1.0</td>
        </tr>
    """.trimIndent()

    private fun product(price: String, unit: String): String = """
        <link rel="canonical" href="https://alanchand.com/currencies-price/usd">
        <script type="application/ld+json">{"@type":"Product","sku":"USD","offers":{
          "price":"$price","priceCurrency":"$unit","url":"https://alanchand.com/currencies-price/usd",
          "priceValidUntil":"2026-10-27T00:15:07+03:30"}}</script>
    """.trimIndent()
}
