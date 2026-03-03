package com.ultraguard.admin

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.UserManager
import android.provider.Settings
import android.util.Log
import com.ultraguard.dns.DnsEnforcer
import com.ultraguard.dns.DnsMonitorService
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
        // Locks DNS to family-safe DNS with automatic fallback.
        DnsEnforcer.enforcePrivateDns(context, dpm, admin)

        // ── Layer 2: User restrictions (prevent bypass) ──
        enforceUserRestrictions(dpm, admin)

        // ── Layer 3: Disable developer options ──
        // ADB setup turns on developer options; some apps (banking, etc.)
        // refuse to run while it's enabled. This turns it off.
        disableDeveloperOptions(dpm, admin)

        // ── Layer 6: Schedule watchdog (15-min safety net) ──
        WatchdogScheduler.schedulePeriodicCheck(context)

        // ── Layer 7: Start DNS monitor service (instant detection) ──
        try {
            DnsMonitorService.start(context)
        } catch (e: Exception) {
            Log.w(TAG, "Could not start DNS monitor service: ${e.message}")
        }

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

    // ────────────────────────────────────────────────────────────
    //  Layer 3: Disable developer options
    // ────────────────────────────────────────────────────────────
    //  Problem: ADB setup requires developer options ON.
    //  After setup, DISALLOW_DEBUGGING_FEATURES blocks the user
    //  from toggling it OFF manually. But some apps (banking,
    //  streaming, etc.) refuse to run when dev options is enabled.
    //
    //  Solution: As device owner, programmatically set
    //  Settings.Global.DEVELOPMENT_SETTINGS_ENABLED = 0
    //  This turns off developer options without needing user action.
    // ────────────────────────────────────────────────────────────
    private fun disableDeveloperOptions(dpm: DevicePolicyManager, admin: ComponentName) {
        try {
            dpm.setGlobalSetting(admin, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, "0")
            Log.i(TAG, "Developer options disabled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable developer options: ${e.message}")
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
