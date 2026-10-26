package com.chand.mobiletina.data

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HanshaParserTest {
    private val now = Instant.parse("2026-10-26T20:00:00Z").toEpochMilli()

    @Test
    fun selectsUsdSaleNotBuyTetherEuroOrRialConversion() {
        val body = """[
            {"ab":"usdt","ps":[{"sp":999999,"ts":"2026-10-26T19:50:00Z"}]},
            {"ab":"eur","ps":[{"sp":888888,"ts":"2026-10-26T19:50:00Z"}]},
            {"ab":"usd","ps":[{"bp":270300,"sp":270500,"ts":"2026-10-26T19:30:00Z"}]}
        ]"""
        assertEquals(270_500L, HanshaParser.usdSellToman(body, now))
    }

    @Test
    fun selectsNewestTimestampEvenWhenHistoryIsUnsorted() {
        assertEquals(270_500L, parse("""
            {"sp":271000,"ts":"2026-10-26T17:00:00Z"},
            {"sp":270500,"ts":"2026-10-26T19:30:00Z"},
            {"sp":270800,"ts":"2026-10-26T18:00:00Z"}
        """))
    }

    @Test
    fun rejectsInvalidLatestQuoteInsteadOfSilentlyUsingOlderHistory() {
        for (price in listOf("0", "-1", "null", "true", "\"—\"", "270500.5", "9223372036854775808")) {
            assertNull(price, parse("""
                {"sp":270800,"ts":"2026-10-26T18:00:00Z"},
                {"sp":$price,"ts":"2026-10-26T19:30:00Z"}
            """))
        }
    }

    @Test
    fun acceptsExactIntegerStringsAndOffsetTimestamps() {
        assertEquals(270_500L, parse("""
            {"sp":"270500.00","ts":"2026-10-26T23:00:00+03:30"}
        """))
    }

    @Test
    fun rejectsMalformedMissingAndOtherCurrencyPayloads() {
        for (body in listOf("", "<html>Unavailable</html>", "{", "{}", "[]", "[null]",
            """[{"ab":"usdt","ps":[{"sp":270500,"ts":"2026-10-26T19:30:00Z"}]}]""",
            """[{"ab":"usd","ps":null}]""",
            """[{"ab":"usd","ps":[{"bp":270500,"ts":"2026-10-26T19:30:00Z"}]}]""")) {
            assertNull(body, HanshaParser.usdSellToman(body, now))
        }
    }

    @Test
    fun rejectsStaleFutureAndInvalidTimestamps() {
        for (time in listOf("2026-10-22T19:00:00Z", "2026-10-26T20:06:00Z", "bad", "")) {
            assertNull(time, parse("""{"sp":270500,"ts":"$time"}"""))
        }
    }

    @Test
    fun acceptsMarketWeekendButNotArbitraryHistory() {
        assertEquals(270_500L, parse("""{"sp":270500,"ts":"2026-10-24T20:00:00Z"}"""))
    }

    @Test
    fun handlesUsdCaseAndDuplicateRowsByTime() {
        assertEquals(270_500L, HanshaParser.usdSellToman("""[
            {"ab":"usd","ps":[{"sp":270800,"ts":"2026-10-26T18:00:00Z"}]},
            {"ab":" USD ","ps":[{"sp":270500,"ts":"2026-10-26T19:30:00Z"}]}
        ]""", now))
    }

    private fun parse(prices: String): Long? =
        HanshaParser.usdSellToman("""[{"ab":"usd","ps":[$prices]}]""", now)
}
