package com.ultraguard.violation

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.ultraguard.admin.AdminReceiver
import com.ultraguard.analytics.EventLogger

/**
 * ═══════════════════════════════════════════════════════════════
 *  LAYER 5 — VIOLATION ENFORCEMENT (Non-Aggressive)
 * ═══════════════════════════════════════════════════════════════
 *
 *  Escalation ladder:
 *    1. Blocked request detected → log event
 *    2. Multiple attempts → show warning notification
 *    3. Continued attempts → temporarily block offending app
 *    4. Severe/persistent → device lock
 *
 *  No permanent restrictions. No kiosk mode. No screen lock abuse.
 */
object ViolationEnforcer {

    private const val TAG = "OmegaViolation"

    // Track violations per session
    private var violationCount = 0
    private var lastViolationTime = 0L

    // Escalation thresholds
    private const val WARN_THRESHOLD = 3        // Show warning after 3 attempts
    private const val APP_BLOCK_THRESHOLD = 10   // Hide offending app after 10 attempts
    private const val LOCK_THRESHOLD = 20        // Lock device after 20 attempts

    // Reset window (ms) — violations reset after 30 minutes of no activity
    private const val RESET_WINDOW_MS = 30 * 60 * 1000L

    /**
     * Called whenever a blocked domain access is detected.
     */
    fun onViolation(context: Context, domain: String, sourceApp: String?) {
        val now = System.currentTimeMillis()

        // Reset counter if cooldown has passed
        if (now - lastViolationTime > RESET_WINDOW_MS) {
            violationCount = 0
        }

        violationCount++
        lastViolationTime = now

        Log.i(TAG, "Violation #$violationCount: $domain (from: ${sourceApp ?: "unknown"})")

        // Log the event
        EventLogger.log(context, domain, sourceApp ?: "unknown")

        // Escalation ladder
        when {
            violationCount >= LOCK_THRESHOLD -> {
                Log.w(TAG, "Lock threshold reached — locking device")
                lockDevice(context)
            }
            violationCount >= APP_BLOCK_THRESHOLD -> {
                Log.w(TAG, "App block threshold reached")
                if (sourceApp != null) {
                    com.ultraguard.appcontrol.AppVisibilityManager.hideApp(context, sourceApp)
                }
            }
            violationCount >= WARN_THRESHOLD -> {
                Log.i(TAG, "Warning threshold reached — sending notification")
                sendWarningNotification(context, violationCount)
            }
        }
    }

    /**
     * Lock the device screen (requires Device Admin).
     */
    private fun lockDevice(context: Context) {
        try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            dpm.lockNow()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to lock device: ${e.message}")
        }
    }

    /**
     * Send a warning notification to the user.
     */
    private fun sendWarningNotification(context: Context, count: Int) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as android.app.NotificationManager

            val notification = android.app.Notification.Builder(context, "omega_guard_channel")
                .setContentTitle("Omega Lite Warning")
                .setContentText("$count blocked access attempts detected. Stay strong.")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setAutoCancel(true)
                .build()

            nm.notify(1000, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send warning: ${e.message}")
        }
    }

    /**
     * Get current violation count (for analytics display).
     */
    fun getViolationCount(): Int = violationCount

    /**
     * Manually reset violations (e.g., after a cooldown period).
     */
    fun resetViolations() {
        violationCount = 0
        lastViolationTime = 0L
    }
}
