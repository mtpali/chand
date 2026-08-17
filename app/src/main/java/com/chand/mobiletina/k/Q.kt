package com.chand.mobiletina.k

internal object Q {
    private val loaded: Boolean = runCatching {
        System.loadLibrary("cndx")
        true
    }.getOrDefault(false)

    private external fun v(slot: Int): ByteArray
    private external fun w(): Int

    fun t(slot: Int): String {
        if (!loaded) return ""
        return runCatching { String(v(slot), Charsets.UTF_8) }.getOrDefault("")
    }

    fun h(): Boolean {
        if (!loaded) return false
        return runCatching { w() == 0 }.getOrDefault(false)
    }
}
