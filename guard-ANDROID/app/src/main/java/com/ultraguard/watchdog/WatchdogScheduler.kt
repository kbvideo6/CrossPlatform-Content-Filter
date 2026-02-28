package com.ultraguard.watchdog

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Schedules the periodic watchdog that verifies all policies
 * remain enforced. Uses WorkManager for reliable execution.
 */
object WatchdogScheduler {

    private const val TAG = "OmegaWatchdogSched"
    private const val WORK_NAME = "omega_watchdog_periodic"

    /**
     * Schedule a periodic check every 15 minutes (minimum WorkManager interval).
     * The check will:
     * - Confirm Device Owner status
     * - Re-apply Private DNS settings
     * - Reapply policies if anything is broken
     */
    fun schedulePeriodicCheck(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<WatchdogWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        Log.i(TAG, "Watchdog periodic check scheduled (every 15 min)")
    }
}
