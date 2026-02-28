package com.ultraguard.admin

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * ═══════════════════════════════════════════════════════════════
 *  LAYER 1 — DEVICE OWNER RECEIVER
 * ═══════════════════════════════════════════════════════════════
 *
 *  Provisioned via ADB:
 *    adb shell dpm set-device-owner com.ultraguard/.admin.AdminReceiver
 *
 *  This gives the app full DevicePolicyManager authority while
 *  keeping the device fully usable (no kiosk, no screen lock abuse).
 */
class AdminReceiver : DeviceAdminReceiver() {

    companion object {
        private const val TAG = "OmegaAdmin"

        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, AdminReceiver::class.java)
        }
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "Device admin enabled")
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        Log.i(TAG, "Profile provisioning complete — enforcing policies")

        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = getComponentName(context)

        // Enforce all protection layers immediately
        PolicyManager.enforceAllPolicies(context, dpm, admin)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.w(TAG, "Device admin disabled — protection removed")
    }
}
