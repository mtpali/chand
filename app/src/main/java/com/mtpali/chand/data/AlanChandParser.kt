package com.mtpali.chand.data

import org.json.JSONArray
import org.json.JSONObject

object AlanChandParser {
    fun publicHtmlUsdSellRial(html: String): Long? {
        val row = Regex("(?is)<tr[^>]*>.*?(?:US\\s*Dollar|دلار\\s*آمریکا).*?</tr>")
            .findAll(html)
            .map { it.value }
            .firstOrNull { !it.contains("Remittance", ignoreCase = true) && !it.contains("Istanbul", ignoreCase = true) }

        val scope = row ?: run {
            val marker = listOf("US Dollar", "دلار آمریکا")
                .map { html.indexOf(it, ignoreCase = true) }
                .filter { it >= 0 }
                .minOrNull() ?: return null
            html.substring(marker, minOf(html.length, marker + 2200))
        }

        val text = scope
            .replace(Regex("(?is)<[^>]+>"), " ")
            .replace("&nbsp;", " ")
            .replace("&#44;", ",")

        val numbers = Regex("[0-9][0-9,٬]{4,}")
            .findAll(text)
            .mapNotNull { parseInteger(it.value) }
            .filter { it >= 100_000L }
            .toList()

        return when {
            numbers.size >= 2 -> numbers[1]
            numbers.isNotEmpty() -> numbers[0]
            else -> null
        }
    }

    fun officialApiUsdPrice(rawJson: String): Long? {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) return null
        val root: Any = if (trimmed.startsWith("[")) JSONArray(trimmed) else JSONObject(trimmed)
        return findUsdPrice(root)
    }

    fun normalizeToToman(raw: Long): Long {
        // AlanChand's public currency table is denominated in IRR. Some API
        // responses may already expose toman, so keep realistic toman values as-is.
        return if (raw >= 500_000L) raw / 10L else raw
    }

    private fun findUsdPrice(node: Any?): Long? {
        return when (node) {
            is JSONObject -> {
                val keys = node.keys().asSequence().toList()
                for (key in keys) {
                    if (key.equals("usd", ignoreCase = true)) {
                        val direct = node.opt(key)
                        val value = extractNumeric(direct) ?: if (direct is JSONObject) preferredPrice(direct) else null
                        if (value != null) return value
                    }
                }

                if (looksLikeUsd(node)) {
                    preferredPrice(node)?.let { return it }
                }

                for (key in keys) {
                    findUsdPrice(node.opt(key))?.let { return it }
                }
                null
            }
            is JSONArray -> {
                for (i in 0 until node.length()) {
                    findUsdPrice(node.opt(i))?.let { return it }
                }
                null
            }
            else -> null
        }
    }

    private fun looksLikeUsd(obj: JSONObject): Boolean {
        val candidates = listOf("symbol", "code", "slug", "name", "title")
        return candidates.any { key ->
            val value = obj.optString(key, "").trim()
            value.equals("usd", true) || value.contains("US Dollar", true) || value.contains("دلار آمریکا")
        }
    }

    private fun preferredPrice(obj: JSONObject): Long? {
        val keys = listOf(
            "sell_price", "sellPrice", "sell", "ask", "price", "value",
            "buy_price", "buyPrice", "buy"
        )
        for (key in keys) {
            extractNumeric(obj.opt(key))?.let { return it }
        }
        return null
    }

    private fun extractNumeric(value: Any?): Long? = when (value) {
        is Number -> value.toLong()
        is String -> parseInteger(value)
        else -> null
    }

    private fun parseInteger(value: String): Long? {
        val cleaned = value.replace(Regex("[^0-9]"), "")
        return cleaned.toLongOrNull()
    }
}
