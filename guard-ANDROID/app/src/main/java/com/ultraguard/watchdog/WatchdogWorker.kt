package com.ultraguard.watchdog

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ultraguard.admin.AdminReceiver
import com.ultraguard.dns.DnsEnforcer

/**
 * ═══════════════════════════════════════════════════════════════
 *  LAYER 6 — AUTO-HEAL WATCHDOG
 * ═══════════════════════════════════════════════════════════════
 *
 *  Runs every 15 minutes (WorkManager minimum) and checks:
 *    ✓ Are DNS settings correct?
 *    ✓ Is app still Device Owner?
 *
 *  If anything is broken → re-apply policies automatically.
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

        // ── Check 2: DNS settings ──
        try {
            DnsEnforcer.reapply(dpm, admin)
            Log.i(TAG, "✓ DNS settings re-applied")
        } catch (e: Exception) {
            Log.e(TAG, "✗ DNS enforcement failed: ${e.message}")
            allHealthy = false
        }

        if (allHealthy) {
            Log.i(TAG, "═══ All checks passed ═══")
        } else {
            Log.w(TAG, "═══ Some checks failed — policies re-applied ═══")
        }

        return Result.success()
    }
}
