package com.chand.mobiletina.data

/** Runtime endpoint obfuscation; these public addresses are metadata, not authentication secrets. */
internal object SourceEndpoints {
    val hanshaRates: String by lazy { reveal(intArrayOf(59, 12, 233, 178, 148, 54, 30, 121, 19, 193, 171, 153, 103, 85, 119, 17, 205, 164, 132, 124, 82, 115, 237, 148), 83) }
    val alanHome: String by lazy { reveal(intArrayOf(255, 200, 149, 118, 88, 106, 90, 181, 222, 136, 104, 64, 48, 16, 252, 172, 131, 34, 82, 57, 22, 143), 151) }
    val alanUsd: String by lazy { reveal(intArrayOf(169, 146, 127, 64, 38, 64, 176, 235, 136, 98, 82, 54, 30, 202, 166, 130, 117, 24, 56, 239, 200, 229, 140, 97, 75, 44, 230, 198, 174, 155, 114, 79, 76, 246, 217, 185, 150, 127, 16, 17, 250, 202), 193) }
    val tgjuUsd: String by lazy { reveal(intArrayOf(89, 34, 15, 208, 182, 208, 32, 27, 46, 9, 212, 230, 153, 117, 93, 41, 175, 201, 185, 151, 58, 74, 45, 235, 207, 167, 159, 125, 18, 18, 245, 197, 178, 147, 68, 36, 10, 230, 195, 181, 139, 65, 49, 4), 49) }
    val tgjuCurrencies: String by lazy { reveal(intArrayOf(223, 168, 117, 86, 56, 74, 186, 149, 168, 115, 94, 96, 7, 255, 215, 151, 41, 67, 35, 17, 180, 163, 144, 120, 93, 49, 23, 253, 186), 183) }

    private fun reveal(values: IntArray, seed: Int): String = String(
        CharArray(values.size) { i -> (values[i] xor ((seed + i * 37) and 0xff)).toChar() }
    )
}
