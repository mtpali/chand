package com.chand.mobiletina

import android.app.Application
import com.chand.mobiletina.security.IntegrityGuard

/**
 * Verify hardened builds before an activity, widget receiver or worker can run.
 * WorkManager scheduling stays in the receivers and activity to keep startup lightweight.
 */
class ChandApp : Application() {
    override fun onCreate() {
        super.onCreate()
        check(IntegrityGuard.verify(this)) { "Application integrity verification failed" }
    }
}
