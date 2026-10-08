package com.chand.mobiletina.data

object TgjuParser {
    // Quoted attributes can contain HTML tooltips, including > and other numbers.
    private val tags = Regex("""(?is)<(?:link|span|tr)\b(?:[^"'<>]|"[^"]*"|'[^']*')*>""")
    private val attributes = Regex("""(?is)([a-z_:][-a-z0-9_:.]*)\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s>]+))""")
    private val ignored = Regex("""(?is)<!--.*?-->|<(script|style)\b[^>]*>.*?</\1\s*>""")
    private val numericEntities = Regex("""&#(x[0-9a-f]+|[0-9]+);""", RegexOption.IGNORE_CASE)
    private val integer = Regex("""[0-9]+(?:[,٬\s]+[0-9]+)*""")

    fun publicHtmlUsdRial(html: String): Long? {
        val markup = ignored.replace(html, "")
        val elements = tags.findAll(markup).toList()
        val isDollarProfile = elements.any {
            it.value.startsWith("<link", ignoreCase = true) &&
                attribute(it.value, "rel").equals("canonical", ignoreCase = true) &&
                attribute(it.value, "href")?.trimEnd('/') in setOf(
                    "https://www.tgju.org/profile/price_dollar_rl",
                    "https://tgju.org/profile/price_dollar_rl",
                    "/profile/price_dollar_rl"
                )
        }

        if (isDollarProfile) {
            for (tag in elements) {
                if (!tag.value.startsWith("<span", ignoreCase = true) ||
                    attribute(tag.value, "data-col") != "info.last_trade.PDrCotVal"
                ) continue
                val start = tag.range.last + 1
                val end = markup.indexOf("</span", start, ignoreCase = true)
                if (end < 0) continue
                parseInteger(markup.substring(start, end))?.let { return it }
            }
        }

        // The public currency table is a second route if the profile is unavailable.
        // Read only the USD row's current-price attribute, never a neighbouring rate.
        for (tag in elements) {
            if (tag.value.startsWith("<tr", ignoreCase = true) &&
                attribute(tag.value, "data-market-row") == "price_dollar_rl"
            ) {
                attribute(tag.value, "data-price")?.let(::parseInteger)?.let { return it }
            }
        }
        return null
    }

    fun normalizeToToman(rawRial: Long): Long = rawRial / 10L

    private fun attribute(tag: String, name: String): String? = attributes.findAll(tag)
        .firstOrNull { it.groupValues[1].equals(name, ignoreCase = true) }
        ?.let { match -> match.groupValues.drop(2).firstOrNull { it.isNotEmpty() } }

    private fun parseInteger(value: String): Long? {
        val text = numericEntities.replace(value) { match ->
            val entity = match.groupValues[1]
            val number = if (entity.startsWith("x", ignoreCase = true)) {
                entity.drop(1).toIntOrNull(16)
            } else entity.toIntOrNull()
            if (number != null && number in 0..Char.MAX_VALUE.code) number.toChar().toString()
            else match.value
        }.replace("&nbsp;", " ")
            .replace(Regex("""(?s)<[^>]*>"""), " ")
            .map { char ->
                when (char) {
                    in '۰'..'۹' -> '0' + (char - '۰')
                    in '٠'..'٩' -> '0' + (char - '٠')
                    else -> char
                }
            }.joinToString("").trim()
        if (!integer.matches(text)) return null
        return text.filter { it in '0'..'9' }.toLongOrNull()?.takeIf { it > 0L }
    }
}
