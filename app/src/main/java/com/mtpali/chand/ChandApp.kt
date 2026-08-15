package com.mtpali.chand

import android.app.Application
import com.mtpali.chand.work.PriceUpdateScheduler

class ChandApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PriceUpdateScheduler.schedule(this)
    }
}
