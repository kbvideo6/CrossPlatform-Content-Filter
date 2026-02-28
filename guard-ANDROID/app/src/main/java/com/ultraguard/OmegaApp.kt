package com.ultraguard

import android.app.Application
import com.ultraguard.watchdog.WatchdogScheduler

/**
 * Application entry point.
 * Schedules the watchdog worker on app start.
 */
class OmegaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Schedule periodic watchdog to verify all policies remain enforced
        WatchdogScheduler.schedulePeriodicCheck(this)
    }
}
