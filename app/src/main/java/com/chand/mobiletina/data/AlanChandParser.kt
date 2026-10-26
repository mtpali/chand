package com.chand.mobiletina.data

import java.time.Instant
import org.json.JSONObject

object AlanChandParser {
    private val ignored = Regex("""(?is)<!--.*?-->|<(script|style)\b[^>]*>.*?</\1\s*>""")
    private val rows = Regex("""(?is)<tr\b(?:[^"'<>]|"[^"]*"|'[^']*')*>(.*?)</tr\s*>""")
    private val cells = Regex("""(?is)<td\b((?:[^"'<>]|"[^"]*"|'[^']*')*)>(.*?)</td\s*>""")
    private val attributes = Regex("""(?is)([a-z_:][-a-z0-9_:.]*)\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s>]+))""")
    private val scripts = Regex("""(?is)<script\b((?:[^"'<>]|"[^"]*"|'[^']*')*)>(.*?)</script\s*>""")
    private val links = Regex("""(?is)<link\b((?:[^"'<>]|"[^"]*"|'[^']*')*)>""")
    private val entities = Regex("""&#(x[0-9a-f]+|[0-9]+);""", RegexOption.IGNORE_CASE)
    private val integer = Regex("""[0-9]+(?:[,٬\s]+[0-9]+)*""")

    /** The visible homepage sellPrice cell is already in toman. */
    fun homeUsdSellToman(html: String): Long? {
        val markup = ignored.replace(html, "")
        for (row in rows.findAll(markup)) {
            val columns = cells.findAll(row.groupValues[1]).toList()
            val name = columns.firstOrNull { hasClass(it.groupValues[1], "currName") }
                ?.let { text(it.groupValues[2]) }
                ?.replace('ي', 'ی')?.replace('ك', 'ک') ?: continue
            if (name != "دلار آمریکا" && !name.equals("US Dollar", ignoreCase = true)) continue
            // Do not infer a price from history, the buy column, or a neighbouring currency.
            val sell = columns.firstOrNull { hasClass(it.groupValues[1], "sellPrice") } ?: continue
            return positiveInteger(text(sell.groupValues[2]))
        }
        return null
    }

    /** The dedicated USD page exposes an explicit currency unit in its Product/Offer JSON-LD. */
    fun productUsdSellToman(html: String, nowMillis: Long = System.currentTimeMillis()): Long? {
        val withoutComments = html.replace(Regex("(?s)<!--.*?-->"), "")
        val canonical = links.findAll(withoutComments).firstOrNull {
            attribute(it.groupValues[1], "rel").equals("canonical", ignoreCase = true)
        }?.let { attribute(it.groupValues[1], "href") }
        if (!isUsdUrl(canonical)) return null
        for (script in scripts.findAll(withoutComments)) {
            if (!attribute(script.groupValues[1], "type").equals("application/ld+json", ignoreCase = true)) continue
            val product = runCatching { JSONObject(script.groupValues[2]) }.getOrNull() ?: continue
            if (product.optString("@type") != "Product" || !product.optString("sku").equals("USD", ignoreCase = true)) continue
            val offer = product.optJSONObject("offers") ?: continue
            if (!isUsdUrl(offer.optString("url"))) continue
            val expiry = offer.optString("priceValidUntil")
            if (expiry.isNotBlank()) {
                val expires = runCatching { Instant.parse(expiry).toEpochMilli() }.getOrNull() ?: continue
                if (expires < nowMillis) continue
            }
            val raw = runCatching { offer.optString("price").toBigDecimal().longValueExact() }
                .getOrNull()?.takeIf { it > 0L } ?: continue
            val toman = when (offer.optString("priceCurrency")) {
                "IRR" -> raw / 10L
                "IRT" -> raw
                else -> continue
            }
            return toman.takeIf { it > 0L }
        }
        return null
    }

    private fun isUsdUrl(value: String?): Boolean = value?.trimEnd('/') in setOf(
        AlanChandClient.PUBLIC_USD_PAGE, AlanChandClient.PUBLIC_USD_PAGE.replace("https://", "https://www."),
        "/currencies-price/usd"
    )

    private fun hasClass(attributes: String, name: String): Boolean =
        attribute(attributes, "class")?.split(Regex("\\s+"))?.any { it == name } == true

    private fun attribute(tag: String, name: String): String? = attributes.findAll(tag)
        .firstOrNull { it.groupValues[1].equals(name, ignoreCase = true) }
        ?.let { it.groupValues.drop(2).firstOrNull(String::isNotEmpty) }

    private fun text(value: String): String = entities.replace(value) {
        val raw = it.groupValues[1]
        val number = if (raw.startsWith("x", true)) raw.drop(1).toIntOrNull(16) else raw.toIntOrNull()
        if (number != null && number in 0..Char.MAX_VALUE.code) number.toChar().toString() else it.value
    }.replace("&nbsp;", " ").replace(Regex("(?s)<[^>]*>"), " ")
        .replace(Regex("\\s+"), " ").trim()

    private fun positiveInteger(value: String): Long? {
        val normalized = value.map {
            when (it) {
                in '۰'..'۹' -> '0' + (it - '۰')
                in '٠'..'٩' -> '0' + (it - '٠')
                else -> it
            }
        }.joinToString("")
        if (!integer.matches(normalized)) return null
        return normalized.filter { it in '0'..'9' }.toLongOrNull()?.takeIf { it > 0L }
    }
}
