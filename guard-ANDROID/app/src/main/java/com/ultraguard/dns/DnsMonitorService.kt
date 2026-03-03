package com.ultraguard.dns

import android.app.*
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import com.ultraguard.admin.AdminReceiver

/**
 * ═══════════════════════════════════════════════════════════════
 *  DNS MONITOR SERVICE — Reactive DNS tamper detection
 * ═══════════════════════════════════════════════════════════════
 *
 *  Foreground service that uses a ContentObserver on
 *  Settings.Global("private_dns_mode") and ("private_dns_specifier").
 *
 *  When ANY change is detected, the DNS is immediately re-applied.
 *  This is event-driven (like a button click listener), NOT polling,
 *  so it uses virtually zero battery/CPU.
 *
 *  The 15-min WatchdogWorker still runs as a safety net, but this
 *  service handles the instant response.
 */
class DnsMonitorService : Service() {

    companion object {
        private const val TAG = "OmegaDnsMonitor"
        const val CHANNEL_ID = "omega_dns_monitor"
        private const val NOTIFICATION_ID = 2001

        fun start(context: Context) {
            val intent = Intent(context, DnsMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private var dnsObserver: ContentObserver? = null

    /**
     * Guard flag to prevent re-entrant enforcement.
     * When we re-apply DNS, the ContentObserver fires again —
     * this flag prevents an infinite loop.
     */
    @Volatile
    private var isEnforcing = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        registerDnsObserver()
        Log.i(TAG, "DNS Monitor Service started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY = restart automatically if killed by the system
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        dnsObserver?.let { contentResolver.unregisterContentObserver(it) }
        Log.w(TAG, "DNS Monitor Service destroyed — should be restarted")
    }

    // ────────────────────────────────────────────────────────────
    //  ContentObserver — reacts to DNS setting changes instantly
    // ────────────────────────────────────────────────────────────
    private fun registerDnsObserver() {
        val handler = Handler(Looper.getMainLooper())

        dnsObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                Log.i(TAG, "DNS setting change detected: $uri")
                onDnsSettingChanged()
            }
        }

        // Watch private_dns_mode (off / opportunistic / hostname)
        contentResolver.registerContentObserver(
            Settings.Global.getUriFor("private_dns_mode"),
            false,
            dnsObserver!!
        )

        // Watch private_dns_specifier (the actual hostname)
        contentResolver.registerContentObserver(
            Settings.Global.getUriFor("private_dns_specifier"),
            false,
            dnsObserver!!
        )

        Log.i(TAG, "ContentObserver registered on private_dns_mode + private_dns_specifier")
    }

    private fun onDnsSettingChanged() {
        // Prevent re-entrant calls (our own enforcement triggers onChange)
        if (isEnforcing) return
        isEnforcing = true

        try {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin = AdminReceiver.getComponentName(this)

            if (!dpm.isDeviceOwnerApp(packageName)) {
                Log.w(TAG, "Not device owner — cannot enforce")
                return
            }

            val currentMode = Settings.Global.getString(contentResolver, "private_dns_mode") ?: ""
            val currentHost = Settings.Global.getString(contentResolver, "private_dns_specifier") ?: ""
            val expectedHost = DnsEnforcer.getActiveDnsHost(this)

            if (currentMode != "hostname" || currentHost != expectedHost) {
                Log.w(TAG, "DNS tampered! mode=$currentMode host=$currentHost → re-enforcing to $expectedHost")
                DnsEnforcer.enforcePrivateDns(this, dpm, admin)
            } else {
                Log.d(TAG, "DNS settings OK — no action needed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling DNS change: ${e.message}")
        } finally {
            // Allow re-checks after a brief cooldown (avoids rapid-fire loop)
            Handler(Looper.getMainLooper()).postDelayed({ isEnforcing = false }, 2000)
        }
    }

    // ────────────────────────────────────────────────────────────
    //  Notification (required for foreground service)
    // ────────────────────────────────────────────────────────────
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DNS Protection Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors DNS settings to prevent tampering"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("DNS Protection Active")
                .setContentText("Monitoring DNS settings for changes")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("DNS Protection Active")
                .setContentText("Monitoring DNS settings for changes")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setOngoing(true)
                .build()
        }
    }
}
