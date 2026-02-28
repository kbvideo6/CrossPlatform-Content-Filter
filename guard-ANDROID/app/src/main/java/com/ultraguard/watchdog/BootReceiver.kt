package com.ultraguard.watchdog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.admin.DevicePolicyManager
import android.util.Log
import com.ultraguard.admin.AdminReceiver
import com.ultraguard.admin.PolicyManager

/**
 * Receives BOOT_COMPLETED broadcast and re-applies all policies.
 * Ensures protection survives device reboot.
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
        }
    }
}
