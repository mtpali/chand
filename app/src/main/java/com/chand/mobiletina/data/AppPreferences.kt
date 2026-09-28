package com.chand.mobiletina.data

import android.content.Context

enum class WidgetThemeMode {
    AUTO, LIGHT, DARK;

    fun isDark(systemDark: Boolean): Boolean = when (this) {
        AUTO -> systemDark
        LIGHT -> false
        DARK -> true
    }
}

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("chand_preferences", Context.MODE_PRIVATE)

    fun widgetTheme(): WidgetThemeMode = runCatching {
        WidgetThemeMode.valueOf(prefs.getString(KEY_WIDGET_THEME, WidgetThemeMode.LIGHT.name)!!)
    }.getOrDefault(WidgetThemeMode.LIGHT)

    fun setWidgetTheme(mode: WidgetThemeMode) {
        prefs.edit().putString(KEY_WIDGET_THEME, mode.name).apply()
    }

    fun cachedDollarRate(): DollarRate? {
        val current = prefs.getLong(KEY_RATE_CURRENT, 0L)
        if (current <= 0L) return null
        val previous = prefs.getLong(KEY_RATE_PREVIOUS, current)
        return DollarRate(
            priceToman = current,
            previousToman = previous,
            updatedAtMillis = prefs.getLong(KEY_RATE_UPDATED_AT, 0L),
            source = prefs.getString(KEY_RATE_SOURCE, "") ?: ""
        )
    }

    fun saveDollarRate(
        priceToman: Long,
        source: String,
        nowMillis: Long = System.currentTimeMillis()
    ): DollarRate {
        require(priceToman > 0L)
        val old = cachedDollarRate()
        val rate = old?.withFetchedPrice(priceToman, nowMillis, source)
            ?: DollarRate(priceToman, priceToman, nowMillis, source)

        prefs.edit()
            .putLong(KEY_RATE_CURRENT, rate.priceToman)
            .putLong(KEY_RATE_PREVIOUS, rate.previousToman)
            .putLong(KEY_RATE_UPDATED_AT, nowMillis)
            .putString(KEY_RATE_SOURCE, source)
            .apply()

        return rate
    }

    companion object {
        private const val KEY_WIDGET_THEME = "widget_theme"
        private const val KEY_RATE_CURRENT = "rate_current"
        private const val KEY_RATE_PREVIOUS = "rate_previous"
        private const val KEY_RATE_UPDATED_AT = "rate_updated_at"
        private const val KEY_RATE_SOURCE = "rate_source"
    }
}
