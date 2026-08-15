package com.mtpali.chand.promo

import java.nio.charset.StandardCharsets

/**
 * Keeps promotional labels, usernames and URI templates out of the APK string table
 * and out of plain-text source constants. This is deterrence rather than encryption:
 * a determined reverse engineer can still recover runtime values.
 */
object PromoSecrets {
    private fun decode(key: Int, data: IntArray): String {
        val bytes = ByteArray(data.size) { index -> (data[index] xor key).toByte() }
        return String(bytes, StandardCharsets.UTF_8)
    }

    val instagramTitle: String get() = decode(35, intArrayOf(251,132,248,175,250,165,251,144,251,137,251,132,249,140,251,146,251,132,250,166,3,250,166,250,171,251,139,251,132,248,175,250,167,3,251,137,248,175,250,165,251,132))
    val developerTitle: String get() = decode(42, intArrayOf(242,128,243,162,242,153,242,147,243,173,10,242,133,243,173,243,172,242,133,243,173,10,242,130,242,155,243,172,242,141,243,175,243,173))
    val instagramSubtitle: String get() = decode(49, intArrayOf(233,130,232,182,17,233,132,232,176,233,156,232,182,17,233,128,233,130,232,180,234,189))
    val developerSubtitle: String get() = decode(56, intArrayOf(224,159,224,137,224,146,224,144,224,159,224,143,24,225,189,224,139,224,146,225,186,227,180,225,189,24,224,144,224,159,24,224,146,225,176,224,139,224,129,225,191,218,184,180,224,151,225,191,225,190,224,151,225,191))

    val instagramAccounts: List<String>
        get() = listOf(
            decode(63, intArrayOf(82,80,93,86,83,90,17,75,86,81,94)),
            decode(70, intArrayOf(43,41,36,47,42,35,104,50,47,40,39,116)),
            decode(77, intArrayOf(32,34,47,36,33,40,99,57,36,35,44,44))
        )

    val telegramUser: String get() = decode(84, intArrayOf(34,36,58,109,98,103))
    val instagramPackage: String get() = decode(119, intArrayOf(20,24,26,89,30,25,4,3,22,16,5,22,26,89,22,25,19,5,24,30,19))

    fun instagramAppUri(username: String): String =
        decode(91, intArrayOf(50,53,40,47,58,60,41,58,54,97,116,116,46,40,62,41,100,46,40,62,41,53,58,54,62,102)) + username

    fun instagramWebUri(username: String): String =
        decode(98, intArrayOf(10,22,22,18,17,88,77,77,21,21,21,76,11,12,17,22,3,5,16,3,15,76,1,13,15,77)) + username + "/"

    fun telegramAppUri(username: String): String =
        decode(105, intArrayOf(29,14,83,70,70,27,12,26,6,5,31,12,86,13,6,4,8,0,7,84)) + username

    fun telegramWebUri(username: String): String =
        decode(112, intArrayOf(24,4,4,0,3,74,95,95,4,94,29,21,95)) + username
}
