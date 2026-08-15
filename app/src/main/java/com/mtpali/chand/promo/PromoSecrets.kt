package com.mtpali.chand.promo

import com.mtpali.chand.k.Q

object PromoSecrets {
    val instagramTitle: String by lazy(LazyThreadSafetyMode.PUBLICATION) { Q.t(0) }
    val developerTitle: String by lazy(LazyThreadSafetyMode.PUBLICATION) { Q.t(1) }

    val instagramAccounts: List<String> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        listOf(Q.t(2), Q.t(3), Q.t(4)).filter { it.isNotBlank() }
    }

    val telegramUser: String by lazy(LazyThreadSafetyMode.PUBLICATION) { Q.t(5) }
    val instagramPackage: String by lazy(LazyThreadSafetyMode.PUBLICATION) { Q.t(6) }

    fun instagramAppUri(username: String): String = Q.t(7) + username
    fun instagramWebUri(username: String): String = Q.t(8) + username + "/"
    fun telegramAppUri(username: String): String = Q.t(9) + username
    fun telegramWebUri(username: String): String = Q.t(10) + username
}
