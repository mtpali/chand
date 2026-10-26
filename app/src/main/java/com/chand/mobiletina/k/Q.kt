package com.chand.mobiletina.k

import android.content.Context

internal object Q {
    private val loaded: Boolean = runCatching {
        System.loadLibrary("cndx")
        true
    }.getOrDefault(false)

    private external fun v(slot: Int): ByteArray
    private external fun w(context: Context): Int

    fun t(slot: Int): String {
        if (!loaded) return ""
        return runCatching { String(v(slot), Charsets.UTF_8) }.getOrDefault("")
    }

    fun h(context: Context): Boolean {
        if (!loaded) return false
        return runCatching { w(context.applicationContext) == 0 }.getOrDefault(false)
    }
}
