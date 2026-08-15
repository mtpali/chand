package com.mtpali.chand.data

object AlanChandParser {
    fun publicHtmlUsdSellRial(html: String): Long? {
        val row = Regex("(?is)<tr[^>]*>.*?(?:US\\s*Dollar|دلار\\s*آمریکا).*?</tr>")
            .findAll(html)
            .map { it.value }
            .firstOrNull {
                !it.contains("Remittance", ignoreCase = true) &&
                    !it.contains("Istanbul", ignoreCase = true)
            }

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

    fun normalizeToToman(rawRial: Long): Long = rawRial / 10L

    private fun parseInteger(value: String): Long? {
        val cleaned = value.replace(Regex("[^0-9]"), "")
        return cleaned.toLongOrNull()
    }
}
