package com.chand.mobiletina.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AlanChandParserTest {
    @Test
    fun parsesPublicCurrencyTableSellPrice() {
        val html = """
            <table>
              <tr><th>Currency Name</th><th>Buy Price</th><th>Sell Price</th></tr>
              <tr><td>US Dollar</td><td>1,833,500</td><td>1,852,000</td><td>-</td></tr>
              <tr><td>Euro</td><td>2,117,000</td><td>2,138,000</td><td>0.866</td></tr>
            </table>
        """.trimIndent()

        assertEquals(1_852_000L, AlanChandParser.publicHtmlUsdSellRial(html))
        assertEquals(185_200L, AlanChandParser.normalizeToToman(1_852_000L))
    }
}
