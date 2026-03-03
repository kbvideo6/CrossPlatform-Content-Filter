package com.ultraguard.violation

import android.app.admin.DevicePolicyManager
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
 *    Level 0: Normal — no violations
 *    Level 1: Warning — 3+ attempts → push notification
 *    Level 2: App Block — 10+ attempts → hide offending app
 *    Level 3: Device Lock — 20+ attempts → lock screen
 *
 *  All violations trigger an immediate push notification.
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
     * Pushes an immediate notification AND escalates if needed.
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

        // ── ALWAYS push a blocked-access notification ──
        NotificationHelper.notifyBlockedAccess(context, domain, sourceApp)

        // ── Escalation ladder ──
        when {
            violationCount >= LOCK_THRESHOLD -> {
                Log.w(TAG, "Lock threshold reached — locking device")
                NotificationHelper.notifyCritical(context,
                    "Device locked after $violationCount blocked access attempts")
                lockDevice(context)
            }
            violationCount >= APP_BLOCK_THRESHOLD -> {
                Log.w(TAG, "App block threshold reached")
                NotificationHelper.notifyEscalationWarning(context,
                    violationCount, "App Blocked")
                if (sourceApp != null) {
                    com.ultraguard.appcontrol.AppVisibilityManager.hideApp(context, sourceApp)
                }
            }
            violationCount >= WARN_THRESHOLD -> {
                Log.i(TAG, "Warning threshold reached")
                NotificationHelper.notifyEscalationWarning(context,
                    violationCount, "Warning Issued")
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
     * Get the current escalation level (0-3) for UI display.
     */
    fun getEscalationLevel(): Int {
        return when {
            violationCount >= LOCK_THRESHOLD -> 3
            violationCount >= APP_BLOCK_THRESHOLD -> 2
            violationCount >= WARN_THRESHOLD -> 1
            else -> 0
        }
    }

    /**
     * Get current violation count (for analytics display).
     */
    fun getViolationCount(): Int = violationCount

    /**
     * Get time remaining until violation counter resets (in minutes).
     * Returns -1 if no active violations.
     */
    fun getResetMinutesRemaining(): Int {
        if (lastViolationTime == 0L || violationCount == 0) return -1
        val elapsed = System.currentTimeMillis() - lastViolationTime
        val remaining = RESET_WINDOW_MS - elapsed
        return if (remaining > 0) (remaining / (60 * 1000)).toInt() else 0
    }

    /**
     * Manually reset violations (e.g., after a cooldown period).
     */
    fun resetViolations() {
        violationCount = 0
        lastViolationTime = 0L
    }
}
