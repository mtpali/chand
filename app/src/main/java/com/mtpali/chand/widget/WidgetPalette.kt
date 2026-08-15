package com.mtpali.chand.widget

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProviders
import androidx.glance.material3.ColorProviders as Material3ColorProviders
import com.mtpali.chand.data.WidgetThemeMode

object WidgetPalette {
    private val iosLight = lightColorScheme(
        primary = Color(0xFF4F876A),
        error = Color(0xFFC85050),
        surface = Color(0xFFFFFFFF),
        background = Color(0xFFFFFFFF),
        onSurface = Color(0xFF050505),
        onSurfaceVariant = Color(0xFF8E8E93)
    )

    private val lightProviders = Material3ColorProviders(iosLight)

    // Widget styling is intentionally fixed to a white card with dark typography,
    // independent of the phone/app dark theme, to match the iOS reference.
    @Suppress("UNUSED_PARAMETER")
    fun colors(mode: WidgetThemeMode): ColorProviders = lightProviders
}
