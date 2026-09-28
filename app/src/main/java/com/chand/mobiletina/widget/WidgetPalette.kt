package com.chand.mobiletina.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import com.chand.mobiletina.data.AppPreferences

internal data class WidgetPalette(
    val card: Int,
    val heading: Int,
    val value: Int,
    val secondary: Int,
    val rise: Int,
    val fall: Int
)

internal object WidgetPalettes {
    private val light = WidgetPalette(
        card = Color.WHITE,
        heading = Color.rgb(5, 5, 5),
        value = Color.BLACK,
        secondary = Color.rgb(136, 136, 141),
        rise = Color.rgb(190, 69, 69),
        fall = Color.rgb(75, 135, 103)
    )

    private val dark = WidgetPalette(
        card = Color.rgb(28, 28, 30),
        heading = Color.rgb(245, 245, 247),
        value = Color.WHITE,
        secondary = Color.rgb(174, 174, 182),
        rise = Color.rgb(255, 111, 111),
        fall = Color.rgb(111, 211, 151)
    )

    fun forContext(context: Context): WidgetPalette {
        val systemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        return if (AppPreferences(context).widgetTheme().isDark(systemDark)) dark else light
    }
}
