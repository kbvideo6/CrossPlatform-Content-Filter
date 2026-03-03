package com.ultraguard.watchdog

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ultraguard.admin.AdminReceiver
import com.ultraguard.dns.DnsEnforcer
import com.ultraguard.dns.DnsMonitorService

/**
 * ═══════════════════════════════════════════════════════════════
 *  LAYER 6 — AUTO-HEAL WATCHDOG
 * ═══════════════════════════════════════════════════════════════
 *
 *  Runs every 15 minutes (WorkManager minimum) and checks:
 *    ✓ Is app still Device Owner?
 *    ✓ Are DNS settings correct? (re-applies if changed)
 *    ✓ Is the DNS server actually healthy? (fallback if down)
 *    ✓ Is developer options disabled?
 *    ✓ Is DnsMonitorService running?
 *
 *  The DnsMonitorService handles instant detection (ContentObserver),
 *  but this watchdog is the safety net that catches anything missed.
 */
class WatchdogWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        private const val TAG = "OmegaWatchdog"
    }

    override fun doWork(): Result {
        Log.i(TAG, "═══ Watchdog check started ═══")

        val context = applicationContext
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = AdminReceiver.getComponentName(context)

        var allHealthy = true

        // ── Check 1: Device Owner status ──
        if (!dpm.isDeviceOwnerApp(context.packageName)) {
            Log.e(TAG, "✗ NOT device owner — cannot enforce policies")
            return Result.failure()
        }
        Log.i(TAG, "✓ Device Owner confirmed")

        // ── Check 2: Re-apply DNS settings ──
        try {
            DnsEnforcer.reapply(context, dpm, admin)
            Log.i(TAG, "✓ DNS settings re-applied")
        } catch (e: Exception) {
            Log.e(TAG, "✗ DNS enforcement failed: ${e.message}")
            allHealthy = false
        }

        // ── Check 3: DNS server health + fallback ──
        try {
            DnsEnforcer.checkAndFallback(context, dpm, admin)
            Log.i(TAG, "✓ DNS health check completed")
        } catch (e: Exception) {
            Log.e(TAG, "✗ DNS health check error: ${e.message}")
            allHealthy = false
        }

        // ── Check 4: Developer options must be OFF ──
        try {
            val devEnabled = Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
            )
            if (devEnabled != 0) {
                Log.w(TAG, "Developer options was ON — disabling")
                dpm.setGlobalSetting(admin, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, "0")
            }
            Log.i(TAG, "✓ Developer options: OFF")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Developer options check failed: ${e.message}")
            allHealthy = false
        }

        // ── Check 5: Ensure DnsMonitorService is running ──
        try {
            DnsMonitorService.start(context)
            Log.i(TAG, "✓ DNS Monitor Service ensured")
        } catch (e: Exception) {
            Log.w(TAG, "Could not start DNS monitor service: ${e.message}")
        }

        if (allHealthy) {
            Log.i(TAG, "═══ All checks passed ═══")
        } else {
            Log.w(TAG, "═══ Some checks failed — policies re-applied ═══")
        }

        return Result.success()
    }
}
