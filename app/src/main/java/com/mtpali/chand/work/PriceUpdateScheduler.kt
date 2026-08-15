package com.mtpali.chand.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object PriceUpdateScheduler {
    // Use a new unique name for the one-hour generation. This avoids mutating the old
    // 15-minute PeriodicWorkRequest in place on vendor WorkManager implementations.
    private const val LEGACY_PERIODIC_NAME = "chand-dollar-periodic"
    private const val HOURLY_PERIODIC_NAME = "chand-dollar-hourly-v1"
    private const val IMMEDIATE_NAME = "chand-dollar-immediate"

    fun schedule(context: Context) {
        runCatching {
            val manager = WorkManager.getInstance(context.applicationContext)
            manager.cancelUniqueWork(LEGACY_PERIODIC_NAME)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<PriceUpdateWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            manager.enqueueUniquePeriodicWork(
                HOURLY_PERIODIC_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    fun enqueueNow(context: Context) {
        runCatching {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<PriceUpdateWorker>()
                .setConstraints(constraints)
                // A widget tap is an explicit user action, so ask WorkManager to run
                // the refresh as soon as possible. If expedited quota is unavailable,
                // it automatically falls back to a normal one-time request.
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                IMMEDIATE_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
