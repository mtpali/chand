package com.mtpali.chand.widget

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProviders
import androidx.glance.material3.ColorProviders as Material3ColorProviders
import com.mtpali.chand.data.WidgetThemeMode

object WidgetPalette {
    private val iosLight = lightColorScheme(
        primary = Color(0xFF4F876A),
        error = Color(0xFFB74949),
        surface = Color(0xFFFFFDFB),
        background = Color(0xFFFFFDFB),
        onSurface = Color(0xFF050505),
        onSurfaceVariant = Color(0xFF8A8A8E)
    )

    private val lightProviders = Material3ColorProviders(iosLight)

    // The widgets intentionally stay light even when the phone is in dark mode.
    // This matches the visual reference and keeps the home-screen cards consistent.
    fun colors(mode: WidgetThemeMode): ColorProviders = lightProviders
}
