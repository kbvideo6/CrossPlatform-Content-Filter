package com.ultraguard.appcontrol

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.ultraguard.admin.AdminReceiver

/**
 * ═══════════════════════════════════════════════════════════════
 *  LAYER 4 — APP VISIBILITY CONTROL (Soft Enforcement)
 * ═══════════════════════════════════════════════════════════════
 *
 *  Instead of kiosk mode, selectively hides/shows apps.
 *  Phone remains fully usable — only specific apps are toggled.
 *
 *  Example use cases:
 *  - Hide browsers during study hours
 *  - Block social media at night
 *  - Permanently hide known bypass apps (VPN clients, etc.)
 *
 *  Requires Device Owner privileges.
 */
object AppVisibilityManager {

    private const val TAG = "OmegaAppCtrl"

    // Apps that can be hidden to prevent bypass attempts
    val KNOWN_BYPASS_APPS = listOf(
        "com.opera.browser",          // Opera has built-in VPN
        "com.opera.mini.native",      // Opera Mini
        "org.torproject.torbrowser",  // Tor Browser
        "com.paloaltonetworks.globalprotect" // Example VPN
    )

    /**
     * Hide a specific app. The app remains installed but is invisible
     * to the user — it won't appear in launcher, recent apps, etc.
     */
    fun hideApp(context: Context, packageName: String): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = AdminReceiver.getComponentName(context)

        return try {
            if (dpm.isDeviceOwnerApp(context.packageName)) {
                dpm.setApplicationHidden(admin, packageName, true)
                Log.i(TAG, "Hidden app: $packageName")
                true
            } else {
                Log.w(TAG, "Not device owner — cannot hide apps")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to hide $packageName: ${e.message}")
            false
        }
    }

    /**
     * Show (unhide) a previously hidden app.
     */
    fun showApp(context: Context, packageName: String): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = AdminReceiver.getComponentName(context)

        return try {
            if (dpm.isDeviceOwnerApp(context.packageName)) {
                dpm.setApplicationHidden(admin, packageName, false)
                Log.i(TAG, "Shown app: $packageName")
                true
            } else false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show $packageName: ${e.message}")
            false
        }
    }

    /**
     * Check if an app is currently hidden.
     */
    fun isAppHidden(context: Context, packageName: String): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = AdminReceiver.getComponentName(context)
        return try {
            dpm.isApplicationHidden(admin, packageName)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Hide all known bypass apps (e.g., browsers with built-in VPNs).
     */
    fun hideBypassApps(context: Context) {
        for (pkg in KNOWN_BYPASS_APPS) {
            hideApp(context, pkg)
        }
    }
}
