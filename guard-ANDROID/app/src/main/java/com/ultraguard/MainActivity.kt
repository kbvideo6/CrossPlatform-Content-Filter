package com.ultraguard

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ultraguard.admin.AdminReceiver
import com.ultraguard.admin.PolicyManager
import com.ultraguard.analytics.EventLogger
import com.ultraguard.dns.DnsEnforcer
import com.ultraguard.dns.DomainBlockList
import org.json.JSONArray

/**
 * Main dashboard activity.
 * Shows protection status, system info, analytics, and block history.
 *
 * NO VPN — protection is DNS-only via Device Owner Private DNS lock.
 * This means the user can freely use any real VPN (NordVPN, etc.)
 * and the DNS filtering still works at the system level.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main)
        } catch (e: Exception) {
            // Fallback if layout inflation fails
            val tv = TextView(this)
            tv.text = "Omega Lite\n\nLayout error: ${e.message}"
            tv.setPadding(32, 64, 32, 32)
            tv.textSize = 16f
            setContentView(tv)
            return
        }

        // Enforce policies if device owner (safe — idempotent)
        try {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin = AdminReceiver.getComponentName(this)
            if (dpm.isDeviceOwnerApp(packageName)) {
                PolicyManager.enforceAllPolicies(this, dpm, admin)
            }
        } catch (_: Exception) {}

        refreshDashboard()
    }

    override fun onResume() {
        super.onResume()
        try { refreshDashboard() } catch (_: Exception) {}
    }

    /**
     * Refresh all dashboard cards with current data.
     */
    private fun refreshDashboard() {
        refreshProtectionStatus()
        refreshAnalytics()
        refreshSystemInfo()
        refreshHistory()
    }

    // ── CARD 1: Protection Status ──
    private fun refreshProtectionStatus() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val isOwner = dpm.isDeviceOwnerApp(packageName)

        findViewById<TextView>(R.id.tvDeviceOwner)?.text =
            if (isOwner) "✓ Device Owner: ACTIVE"
            else "✗ Device Owner: NOT SET"

        // Check current Private DNS setting
        val dnsMode = try {
            Settings.Global.getString(contentResolver, "private_dns_mode") ?: "off"
        } catch (_: Exception) { "unknown" }

        val dnsHost = try {
            Settings.Global.getString(contentResolver, "private_dns_specifier") ?: "none"
        } catch (_: Exception) { "unknown" }

        val expectedHost = DnsEnforcer.getActiveDnsHost(this)
        val dnsLocked = dnsMode == "hostname" && dnsHost == expectedHost
        findViewById<TextView>(R.id.tvDnsStatus)?.text =
            if (dnsLocked) "✓ DNS: LOCKED → $dnsHost"
            else "✗ DNS: $dnsMode ($dnsHost)"

        findViewById<TextView>(R.id.tvBlockedDomains)?.text =
            "✓ Block list: ${DomainBlockList.count()} domains"

        // Developer options status
        val devEnabled = try {
            Settings.Global.getInt(contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
        } catch (_: Exception) { 0 }

        findViewById<TextView>(R.id.tvWatchdog)?.text =
            "✓ Watchdog: active | DNS Monitor: active" +
            if (devEnabled != 0) "\n✗ Developer Options: ON (will be fixed)" else "\n✓ Developer Options: OFF"
    }

    // ── CARD 2: Analytics ──
    private fun refreshAnalytics() {
        val blockedToday = EventLogger.getBlockedToday(this)
        val streak = EventLogger.getCleanStreakDays(this)
        val total = EventLogger.getTotalEvents(this)

        findViewById<TextView>(R.id.tvBlockedCount)?.text = "$blockedToday"
        findViewById<TextView>(R.id.tvStreakCount)?.text = "$streak"
        findViewById<TextView>(R.id.tvTotalCount)?.text = "$total"
    }

    // ── CARD 3: System Info (nerdy data) ──
    private fun refreshSystemInfo() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val isOwner = dpm.isDeviceOwnerApp(packageName)

        val sb = StringBuilder()
        sb.appendLine("Device:     ${Build.MANUFACTURER} ${Build.MODEL}")
        sb.appendLine("Android:    ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        sb.appendLine("Build:      ${Build.DISPLAY}")
        sb.appendLine("Kernel:     ${System.getProperty("os.version") ?: "unknown"}")
        sb.appendLine("───────────────────────────")
        sb.appendLine("Package:    $packageName")
        sb.appendLine("Owner:      $isOwner")
        sb.appendLine("DNS mode:   ${getSetting("private_dns_mode")}")
        sb.appendLine("DNS host:   ${getSetting("private_dns_specifier")}")
        sb.appendLine("Active DNS: ${DnsEnforcer.getActiveDnsHost(this)}")
        sb.appendLine("DNS pool:   ${DnsEnforcer.DNS_SERVERS.size} servers")
        sb.appendLine("───────────────────────────")
        sb.appendLine("Domains:    ${DomainBlockList.count()}")
        sb.appendLine("Events:     ${EventLogger.getTotalEvents(this)}")
        sb.appendLine("Uptime:     ${android.os.SystemClock.elapsedRealtime() / 1000 / 60} min")

        // Show user restrictions
        if (isOwner) {
            sb.appendLine("───────────────────────────")
            sb.appendLine("DISALLOW_FACTORY_RESET: ✓")
            sb.appendLine("DISALLOW_SAFE_BOOT:     ✓")
            sb.appendLine("DISALLOW_ADD_USER:      ✓")
        }

        findViewById<TextView>(R.id.tvSysInfo)?.text = sb.toString().trimEnd()
    }

    // ── CARD 4: Block History ──
    private fun refreshHistory() {
        val events = EventLogger.getAllEvents(this)
        if (events.length() == 0) {
            findViewById<TextView>(R.id.tvHistory)?.text =
                "No blocked requests recorded yet.\n\n" +
                "When a blocked domain is accessed via DNS,\n" +
                "it will appear here with timestamp and source."
            return
        }

        val sb = StringBuilder()
        // Show last 20 events, newest first
        val start = maxOf(0, events.length() - 20)
        for (i in events.length() - 1 downTo start) {
            try {
                val ev = events.getJSONObject(i)
                val ts = ev.optString("timestamp", "?")
                val domain = ev.optString("domain", "?")
                val app = ev.optString("app", "?")
                // Format: short timestamp + domain
                val shortTs = if (ts.length > 16) ts.substring(5, 16) else ts
                sb.appendLine("$shortTs  $domain")
                sb.appendLine("             src: $app")
            } catch (_: Exception) {}
        }

        if (sb.isEmpty()) {
            sb.append("Error reading history")
        }

        findViewById<TextView>(R.id.tvHistory)?.text = sb.toString().trimEnd()
    }

    private fun getSetting(key: String): String {
        return try {
            Settings.Global.getString(contentResolver, key) ?: "not set"
        } catch (_: Exception) { "error" }
    }
}
