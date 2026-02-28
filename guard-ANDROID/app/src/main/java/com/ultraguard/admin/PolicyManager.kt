package com.ultraguard.admin

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.UserManager
import android.util.Log
import com.ultraguard.dns.DnsEnforcer
import com.ultraguard.watchdog.WatchdogScheduler

/**
 * ═══════════════════════════════════════════════════════════════
 *  POLICY MANAGER — Central orchestrator for all protection layers
 * ═══════════════════════════════════════════════════════════════
 *
 *  Called on:
 *   - First provisioning (onProfileProvisioningComplete)
 *   - Every boot (BootReceiver)
 *   - Periodic watchdog check (WatchdogWorker)
 *   - Manual re-enforcement from MainActivity
 */
object PolicyManager {

    private const val TAG = "OmegaPolicyMgr"

    /**
     * Master enforcement function.
     * Applies ALL layers in sequence. Safe to call repeatedly (idempotent).
     */
    fun enforceAllPolicies(context: Context, dpm: DevicePolicyManager, admin: ComponentName) {
        Log.i(TAG, "═══ Enforcing all Omega Lite policies ═══")

        // ── Layer 1: System Private DNS (PRIMARY DEFENSE) ──
        // Locks DNS to Cloudflare Family which blocks adult content.
        // Works system-wide, even through other VPNs.
        // User cannot change this setting.
        DnsEnforcer.enforcePrivateDns(dpm, admin)

        // ── Layer 2: User restrictions (prevent bypass) ──
        enforceUserRestrictions(dpm, admin)

        // ── Layer 6: Schedule watchdog ──
        WatchdogScheduler.schedulePeriodicCheck(context)

        Log.i(TAG, "═══ All policies enforced successfully ═══")
    }

    // ────────────────────────────────────────────────────────────
    //  Layer 2: User restrictions (anti-tamper)
    // ────────────────────────────────────────────────────────────
    private fun enforceUserRestrictions(dpm: DevicePolicyManager, admin: ComponentName) {
        try {
            // Prevent factory reset (primary bypass vector)
            dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)

            // Prevent safe boot (bypasses 3rd party apps)
            dpm.addUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT)

            // Prevent USB debugging changes
            dpm.addUserRestriction(admin, UserManager.DISALLOW_DEBUGGING_FEATURES)

            // Prevent adding new user accounts (bypass vector)
            dpm.addUserRestriction(admin, UserManager.DISALLOW_ADD_USER)

            Log.i(TAG, "User restrictions applied")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply user restrictions: ${e.message}")
        }
    }

    /**
     * Check if this app is the Device Owner.
     */
    fun isDeviceOwner(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isDeviceOwnerApp(context.packageName)
    }
}
