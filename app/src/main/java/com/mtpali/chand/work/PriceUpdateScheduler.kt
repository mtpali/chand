package com.mtpali.chand.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object PriceUpdateScheduler {
    private const val PERIODIC_NAME = "chand-dollar-periodic"
    private const val IMMEDIATE_NAME = "chand-dollar-immediate"

    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<PriceUpdateWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        // UPDATE is important for users upgrading from older builds that used a
        // 15-minute interval; it replaces the persisted schedule with one hour.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun enqueueNow(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<PriceUpdateWorker>()
            .setConstraints(constraints)
            .build()

        // Opening the app can happen repeatedly in a short time. Keep only one
        // immediate refresh in the queue to avoid duplicate network requests.
        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
