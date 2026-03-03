package com.ultraguard.violation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log

/**
 * ═══════════════════════════════════════════════════════════════
 *  NOTIFICATION HELPER — Push notifications for blocked access
 * ═══════════════════════════════════════════════════════════════
 *
 *  Sends immediate push notifications when:
 *   1. A blocked site is accessed (DNS blocks it)
 *   2. User manually tries to access a blocked domain
 *   3. Escalation thresholds are reached
 *
 *  Uses separate notification channels for different severity levels.
 */
object NotificationHelper {

    private const val TAG = "OmegaNotify"

    // Notification channels
    const val CHANNEL_BLOCKED = "omega_blocked_access"
    const val CHANNEL_WARNING = "omega_warning"
    const val CHANNEL_CRITICAL = "omega_critical"

    // Notification IDs (use ranges to allow multiple notifications)
    private const val ID_BLOCKED_BASE = 3000
    private const val ID_WARNING = 4000
    private const val ID_CRITICAL = 5000

    private var blockedNotifCounter = 0

    /**
     * Initialize all notification channels. Call once on app start.
     */
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)

            // Blocked access channel (default importance — shows in status bar)
            val blockedChannel = NotificationChannel(
                CHANNEL_BLOCKED,
                "Blocked Access Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when a blocked site is accessed"
                enableLights(true)
                lightColor = 0xFFEF5350.toInt()
                setShowBadge(true)
            }

            // Warning channel (high importance)
            val warningChannel = NotificationChannel(
                CHANNEL_WARNING,
                "Escalation Warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Warnings when multiple blocked access attempts are detected"
                enableLights(true)
                lightColor = 0xFFFFC107.toInt()
                setShowBadge(true)
            }

            // Critical channel (max importance)
            val criticalChannel = NotificationChannel(
                CHANNEL_CRITICAL,
                "Critical Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts for severe violations"
                enableLights(true)
                lightColor = 0xFFFF0000.toInt()
                setShowBadge(true)
            }

            nm.createNotificationChannel(blockedChannel)
            nm.createNotificationChannel(warningChannel)
            nm.createNotificationChannel(criticalChannel)

            Log.i(TAG, "Notification channels created")
        }
    }

    /**
     * Push a notification when a blocked domain is accessed.
     */
    fun notifyBlockedAccess(context: Context, domain: String, sourceApp: String?) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val title = "⛔ Blocked Access Detected"
            val text = if (sourceApp != null) {
                "$domain was blocked (from: $sourceApp)"
            } else {
                "$domain was blocked by DNS filter"
            }

            val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(context, CHANNEL_BLOCKED)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setAutoCancel(true)
                    .setStyle(Notification.BigTextStyle().bigText(
                        "$text\n\nThis domain is on the block list. " +
                        "The DNS filter prevented this request."
                    ))
                    .build()
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(context)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setAutoCancel(true)
                    .build()
            }

            // Use rotating IDs so multiple notifications can stack
            blockedNotifCounter = (blockedNotifCounter + 1) % 10
            nm.notify(ID_BLOCKED_BASE + blockedNotifCounter, notification)

            Log.d(TAG, "Blocked access notification sent: $domain")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send blocked notification: ${e.message}")
        }
    }

    /**
     * Push a warning notification for escalation.
     */
    fun notifyEscalationWarning(context: Context, violationCount: Int, level: String) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val title = "⚠️ Omega Lite — $level"
            val text = "$violationCount blocked attempts detected. Stay strong."

            val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(context, CHANNEL_WARNING)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setAutoCancel(true)
                    .setStyle(Notification.BigTextStyle().bigText(
                        "$text\n\nEscalation Level: $level\n" +
                        "Counter resets after 30 minutes of no violations."
                    ))
                    .build()
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(context)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setAutoCancel(true)
                    .build()
            }

            nm.notify(ID_WARNING, notification)
            Log.d(TAG, "Escalation warning notification sent: $level")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send warning notification: ${e.message}")
        }
    }

    /**
     * Push a critical notification (device lock, etc.)
     */
    fun notifyCritical(context: Context, message: String) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(context, CHANNEL_CRITICAL)
                    .setContentTitle("🔴 Omega Lite — Critical")
                    .setContentText(message)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setAutoCancel(true)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(context)
                    .setContentTitle("🔴 Omega Lite — Critical")
                    .setContentText(message)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setAutoCancel(true)
                    .build()
            }

            nm.notify(ID_CRITICAL, notification)
            Log.d(TAG, "Critical notification sent: $message")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send critical notification: ${e.message}")
        }
    }
}
