package com.chand.mobiletina.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TgjuParserTest {
    @Test
    fun readsOnlyCurrentDollarProfileRateAndConvertsRialToToman() {
        val html = profile("2,668,050") + """
            <span data-col="info.last_trade.last_change_percentage">1.37</span>
            <table><tr><td>بالاترین قیمت روز</td><td>2,672,000</td></tr></table>
        """
        assertEquals(2_668_050L, TgjuParser.publicHtmlUsdRial(html))
        assertEquals(266_805L, TgjuParser.normalizeToToman(2_668_050L))
    }

    @Test
    fun acceptsPersianAndArabicDigitsAndHtmlEntities() {
        assertEquals(2_668_050L, TgjuParser.publicHtmlUsdRial(profile("۲٬۶۶۸٬۰۵۰")))
        assertEquals(2_668_050L, TgjuParser.publicHtmlUsdRial(profile("٢٬٦٦٨٬٠٥٠")))
        assertEquals(2_668_050L, TgjuParser.publicHtmlUsdRial(profile("2&#44;668&#x2c;050")))
        assertEquals(2_668_050L, TgjuParser.publicHtmlUsdRial(profile("<b>2,668,050</b>")))
    }

    @Test
    fun readsOnlyTheExactUsdTableRowDespiteTooltipMarkup() {
        val html = """
            <table>
              <tr data-market-row="price_eur" data-price="2,993,300"><td>یورو</td></tr>
              <tr data-market-row="price_dollar_rl" data-title="<span data-price='9,999,999'>2,656,850 &gt; yesterday</span>" data-price="2,662,000">
                <th>دلار</th><td class="nf">2,662,000</td><td>30,000</td>
              </tr>
              <tr data-market-row="price_dollar_rl_transfer" data-price="1,000,000"></tr>
            </table>
        """
        assertEquals(2_662_000L, TgjuParser.publicHtmlUsdRial(html))
    }

    @Test
    fun acceptsSingleQuotedAndUnquotedAttributes() {
        val html = """
            <link href='https://www.tgju.org/profile/price_dollar_rl/' rel='canonical'>
            <span class=price data-col='info.last_trade.PDrCotVal'>2,668,050</span>
        """
        assertEquals(2_668_050L, TgjuParser.publicHtmlUsdRial(html))
        assertEquals(2_662_000L, TgjuParser.publicHtmlUsdRial(
            "<table><tr data-market-row=price_dollar_rl data-price=2662000></tr></table>"
        ))
    }

    @Test
    fun rejectsOtherProfilesOfficialRatesAndMissingDollarRows() {
        assertNull(TgjuParser.publicHtmlUsdRial(profile("2,668,050").replace(
            "/profile/price_dollar_rl", "/profile/price_eur"
        )))
        assertNull(TgjuParser.publicHtmlUsdRial("""
            <table><tr data-market-row="price_dollar_bank" data-price="1,773,407"></tr>
            <tr data-market-row="price_eur" data-price="2,993,300"></tr></table>
        """))
        assertNull(TgjuParser.publicHtmlUsdRial("<h1>دلار آمریکا</h1><p>Yesterday: 2,632,000</p>"))
    }

    @Test
    fun neverTreatsScriptOrCommentNumbersAsLivePrices() {
        val hidden = """
            <script>const example = '<tr data-market-row="price_dollar_rl" data-price="9,999,999">';</script>
            <!--<tr data-market-row="price_dollar_rl" data-price="8,888,888">-->
        """
        assertNull(TgjuParser.publicHtmlUsdRial(hidden))
        assertEquals(2_668_050L, TgjuParser.publicHtmlUsdRial(hidden + profile("2,668,050")))
    }

    @Test
    fun rejectsEmptyZeroNegativeFractionalOverflowAndMixedFields() {
        for (value in listOf("—", "", "0", "-2,668,050", "2,668,050.5",
            "9223372036854775808", "2,668,050 (1.37%)")) {
            assertNull(value, TgjuParser.publicHtmlUsdRial(profile(value)))
        }
    }

    private fun profile(price: String): String = """
        <link rel="canonical" href="https://www.tgju.org/profile/price_dollar_rl">
        <span class="price" data-col="info.last_trade.PDrCotVal">$price</span>
    """.trimIndent()
}
