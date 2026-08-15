package com.mtpali.chand.k

internal object Q {
    private val loaded: Boolean = runCatching {
        System.loadLibrary("cndx")
        true
    }.getOrDefault(false)

    private external fun v(slot: Int): ByteArray

    fun t(slot: Int): String {
        if (!loaded) return ""
        return runCatching { String(v(slot), Charsets.UTF_8) }.getOrDefault("")
    }
}
