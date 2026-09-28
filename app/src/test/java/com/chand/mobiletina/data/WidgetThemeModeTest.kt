package com.chand.mobiletina.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetThemeModeTest {
    @Test
    fun explicitThemeOverridesDeviceAndAutoFollowsIt() {
        assertFalse(WidgetThemeMode.LIGHT.isDark(true))
        assertTrue(WidgetThemeMode.DARK.isDark(false))
        assertFalse(WidgetThemeMode.AUTO.isDark(false))
        assertTrue(WidgetThemeMode.AUTO.isDark(true))
    }
}
