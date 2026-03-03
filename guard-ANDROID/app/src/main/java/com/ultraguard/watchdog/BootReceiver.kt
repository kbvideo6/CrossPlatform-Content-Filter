package com.ultraguard.watchdog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.admin.DevicePolicyManager
import android.util.Log
import com.ultraguard.admin.AdminReceiver
import com.ultraguard.admin.PolicyManager
import com.ultraguard.dns.DnsMonitorService

/**
 * Receives BOOT_COMPLETED broadcast and re-applies all policies.
 * Ensures protection survives device reboot.
 * Also starts the DNS monitor service for instant tamper detection.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "OmegaBoot"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(TAG, "Boot completed — re-enforcing policies")

            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin = AdminReceiver.getComponentName(context)

            if (dpm.isDeviceOwnerApp(context.packageName)) {
                PolicyManager.enforceAllPolicies(context, dpm, admin)
            }

            // Start DNS monitor service (instant detection via ContentObserver)
            try {
                DnsMonitorService.start(context)
            } catch (e: Exception) {
                Log.w(TAG, "Could not start DNS monitor service on boot: ${e.message}")
            }
        }
    }
}
