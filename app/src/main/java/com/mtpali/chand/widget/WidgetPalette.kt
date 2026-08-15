package com.mtpali.chand.widget

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProviders
import androidx.glance.material3.ColorProviders as Material3ColorProviders
import com.mtpali.chand.data.WidgetThemeMode

object WidgetPalette {
    private val light = lightColorScheme(
        primary = Color(0xFF4F876A),
        error = Color(0xFFB74949),
        surface = Color(0xFFFCFBF8),
        onSurface = Color(0xFF101114),
        onSurfaceVariant = Color(0xFF74777D)
    )

    private val dark = darkColorScheme(
        primary = Color(0xFF75B893),
        error = Color(0xFFFF8A80),
        surface = Color(0xFF1B1E24),
        onSurface = Color(0xFFF4F4F2),
        onSurfaceVariant = Color(0xFFB4B7BC)
    )

    fun colors(mode: WidgetThemeMode): ColorProviders = when (mode) {
        WidgetThemeMode.AUTO -> Material3ColorProviders(light, dark)
        WidgetThemeMode.LIGHT -> Material3ColorProviders(light)
        WidgetThemeMode.DARK -> Material3ColorProviders(dark)
    }
}
