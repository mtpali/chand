package com.mtpali.chand.promo

object PromoSecrets {
    val instagramTitle: String by lazy(LazyThreadSafetyMode.PUBLICATION) { Bx.t(0) }
    val developerTitle: String by lazy(LazyThreadSafetyMode.PUBLICATION) { Bx.t(1) }

    val instagramAccounts: List<String> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        listOf(Bx.t(2), Bx.t(3), Bx.t(4)).filter { it.isNotBlank() }
    }

    val telegramUser: String by lazy(LazyThreadSafetyMode.PUBLICATION) { Bx.t(5) }
    val instagramPackage: String by lazy(LazyThreadSafetyMode.PUBLICATION) { Bx.t(6) }

    fun instagramAppUri(username: String): String = Bx.t(7) + username
    fun instagramWebUri(username: String): String = Bx.t(8) + username + "/"
    fun telegramAppUri(username: String): String = Bx.t(9) + username
    fun telegramWebUri(username: String): String = Bx.t(10) + username
}
