package com.chand.mobiletina.work

import android.content.Context
import android.os.Process
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chand.mobiletina.security.IntegrityGuard
import java.util.concurrent.TimeUnit

object PriceUpdateScheduler {
    // A new unique name replaces previous schedules without retaining duplicate jobs.
    private const val LEGACY_PERIODIC_NAME = "chand-dollar-periodic"
    private const val HOURLY_PERIODIC_NAME = "chand-dollar-hourly-v1"
    private const val HALF_HOURLY_PERIODIC_NAME = "chand-dollar-half-hourly-v1"
    private const val QUARTER_HOURLY_PERIODIC_NAME = "chand-dollar-quarter-hourly-v1"
    private const val IMMEDIATE_NAME = "chand-dollar-immediate"

    private fun allowed(context: Context): Boolean {
        if (IntegrityGuard.verify(context.applicationContext)) return true
        Process.killProcess(Process.myPid())
        return false
    }

    fun schedule(context: Context) {
        if (!allowed(context)) return
        runCatching {
            val manager = WorkManager.getInstance(context.applicationContext)
            manager.cancelUniqueWork(LEGACY_PERIODIC_NAME)
            manager.cancelUniqueWork(HOURLY_PERIODIC_NAME)
            manager.cancelUniqueWork(HALF_HOURLY_PERIODIC_NAME)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<PriceUpdateWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            manager.enqueueUniquePeriodicWork(
                QUARTER_HOURLY_PERIODIC_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    fun enqueueNow(context: Context) {
        if (!allowed(context)) return
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
