package com.ultraguard

import android.app.Application
import android.util.Log
import com.ultraguard.dns.DnsMonitorService
import com.ultraguard.violation.NotificationHelper
import com.ultraguard.watchdog.WatchdogScheduler

/**
 * Application entry point.
 * Schedules the watchdog worker and starts the DNS monitor service.
 */
class OmegaApp : Application() {

    companion object {
        private const val TAG = "OmegaApp"
    }

    override fun onCreate() {
        super.onCreate()

        // Create notification channels early
        NotificationHelper.createChannels(this)

        // Schedule periodic watchdog (15-min safety net)
        WatchdogScheduler.schedulePeriodicCheck(this)

        // Start DNS monitor service (instant ContentObserver-based detection)
        try {
            DnsMonitorService.start(this)
            Log.i(TAG, "DNS Monitor Service started")
        } catch (e: Exception) {
            Log.w(TAG, "Could not start DNS monitor service on app init: ${e.message}")
        }
    }
}
