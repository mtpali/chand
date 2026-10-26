package com.chand.mobiletina.data

import java.time.Instant
import org.json.JSONArray
import org.json.JSONObject

object HanshaParser {
    // Allow normal market closures, but do not replace a quote with abandoned history.
    private const val MAX_AGE_MILLIS = 72L * 60 * 60 * 1_000
    private const val FUTURE_TOLERANCE_MILLIS = 5L * 60 * 1_000

    /** l2's USD sale price is already in toman. Unlike TGJU, it must NOT be divided by ten. */
    fun usdSellToman(body: String, nowMillis: Long = System.currentTimeMillis()): Long? =
        runCatching {
            val rows = JSONArray(body)
            var newestTime = Long.MIN_VALUE
            var newestPrice: Long? = null
            for (i in 0 until rows.length()) {
                val row = rows.optJSONObject(i) ?: continue
                if (!row.optString("ab").trim().equals("usd", ignoreCase = true)) continue
                val prices = row.optJSONArray("ps") ?: continue
                for (j in 0 until prices.length()) {
                    val quote = prices.optJSONObject(j) ?: continue
                    val time = runCatching { Instant.parse(quote.optString("ts")).toEpochMilli() }
                        .getOrNull() ?: continue
                    if (time > newestTime) {
                        newestTime = time
                        newestPrice = exactPositiveLong(quote.opt("sp"))
                    }
                }
            }
            // An invalid latest price falls back to TGJU, rather than using older history.
            if (newestTime < nowMillis - MAX_AGE_MILLIS ||
                newestTime > nowMillis + FUTURE_TOLERANCE_MILLIS
            ) null else newestPrice
        }.getOrNull()

    private fun exactPositiveLong(value: Any?): Long? {
        if (value == null || value == JSONObject.NULL || value is Boolean) return null
        return runCatching { value.toString().toBigDecimal().longValueExact() }
            .getOrNull()?.takeIf { it > 0L }
    }
}
