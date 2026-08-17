package com.chand.mobiletina.data

import android.content.Context

class DollarRepository(context: Context) {
    private val preferences = AppPreferences(context.applicationContext)
    private val client = AlanChandClient()

    fun cached(): DollarRate? = preferences.cachedDollarRate()

    fun refresh(): DollarRate {
        val fetched = client.fetchUsd()
        return preferences.saveDollarRate(fetched.priceToman, fetched.source)
    }
}
