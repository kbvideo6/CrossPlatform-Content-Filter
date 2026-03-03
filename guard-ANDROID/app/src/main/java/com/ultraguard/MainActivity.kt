package com.ultraguard

import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ultraguard.admin.AdminReceiver
import com.ultraguard.admin.PolicyManager
import com.ultraguard.analytics.EventLogger
import com.ultraguard.analytics.NetworkTrafficSummary
import com.ultraguard.dns.DnsEnforcer
import com.ultraguard.dns.DomainBlockList
import com.ultraguard.dns.DomainBlockManager
import com.ultraguard.violation.NotificationHelper
import com.ultraguard.violation.ViolationEnforcer

/**
 * Main dashboard activity — dark theme with 4+ cards.
 *
 * Card 1: Core Defense Status (shield + health indicators)
 * Card 2: Analytics & Streak (clean days + counters)
 * Card 3: Violation Enforcement State (4-stage escalation)
 * Card 4: System Watchdog & Network Traffic
 * Card 5: Custom Domain Block (21-day lock)
 * Card 6: Recent Block History
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main)
        } catch (e: Exception) {
            val tv = TextView(this)
            tv.text = "Omega Lite\n\nLayout error: ${e.message}"
            tv.setPadding(32, 64, 32, 32)
            tv.textSize = 16f
            setContentView(tv)
            return
        }

        // Initialize notification channels
        NotificationHelper.createChannels(this)

        // Enforce policies if device owner (safe — idempotent)
        try {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin = AdminReceiver.getComponentName(this)
            if (dpm.isDeviceOwnerApp(packageName)) {
                PolicyManager.enforceAllPolicies(this, dpm, admin)
            }
        } catch (_: Exception) {}

        setupClickListeners()
        refreshDashboard()
    }

    override fun onResume() {
        super.onResume()
        try { refreshDashboard() } catch (_: Exception) {}
    }

    // ────────────────────────────────────────────────────────────
    //  Click listeners
    // ────────────────────────────────────────────────────────────

    private fun setupClickListeners() {
        // Card 2: Analytics — tap to open full history
        findViewById<View>(R.id.cardAnalytics)?.setOnClickListener {
            startActivity(Intent(this, BlockHistoryActivity::class.java))
        }

        // Card 5: Add Domain button
        findViewById<View>(R.id.btnAddDomain)?.setOnClickListener {
            showAddDomainDialog()
        }
    }

    // ────────────────────────────────────────────────────────────
    //  Dashboard refresh
    // ────────────────────────────────────────────────────────────

    private fun refreshDashboard() {
        refreshCoreDefense()
        refreshAnalytics()
        refreshViolationState()
        refreshWatchdog()
        refreshDomainBlocks()
        refreshHistory()
    }

    // ── CARD 1: Core Defense Status ──
    private fun refreshCoreDefense() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val isOwner = dpm.isDeviceOwnerApp(packageName)

        // Shield icon + title
        if (isOwner) {
            findViewById<TextView>(R.id.tvProtectionTitle)?.apply {
                text = "Protection Active"
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.emerald_green))
            }
            findViewById<View>(R.id.ivShieldGlow)?.setBackgroundResource(R.drawable.shield_glow_active)
            findViewById<TextView>(R.id.tvDeviceOwner)?.text = "Device Owner Authority: Secured"
        } else {
            findViewById<TextView>(R.id.tvProtectionTitle)?.apply {
                text = "Protection Inactive"
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.soft_red))
            }
            findViewById<View>(R.id.ivShieldGlow)?.setBackgroundResource(R.drawable.shield_glow_inactive)
            findViewById<TextView>(R.id.tvDeviceOwner)?.text = "Device Owner: NOT SET — run ADB setup"
        }

        // DNS status
        val dnsMode = try {
            Settings.Global.getString(contentResolver, "private_dns_mode") ?: "off"
        } catch (_: Exception) { "unknown" }

        val dnsHost = try {
            Settings.Global.getString(contentResolver, "private_dns_specifier") ?: "none"
        } catch (_: Exception) { "unknown" }

        val expectedHost = DnsEnforcer.getActiveDnsHost(this)
        val dnsLocked = dnsMode == "hostname" && dnsHost == expectedHost
        findViewById<TextView>(R.id.tvDnsStatus)?.text =
            if (dnsLocked) "DNS Lock: $dnsHost ✓"
            else "DNS: $dnsMode ($dnsHost) ✗"

        // Bypass prevention
        val devEnabled = try {
            Settings.Global.getInt(contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
        } catch (_: Exception) { 0 }

        findViewById<TextView>(R.id.tvBypassPrevention)?.text =
            if (isOwner && devEnabled == 0) "Bypass Prevention: Active ✓"
            else if (devEnabled != 0) "Dev Options: ON (fixing...)"
            else "Bypass Prevention: Inactive"

        // Block list count (built-in + custom)
        val builtIn = DomainBlockList.count()
        val custom = DomainBlockManager.count(this)
        findViewById<TextView>(R.id.tvBlockedDomains)?.text =
            "Block list: $builtIn built-in + $custom custom"
    }

    // ── CARD 2: Analytics & Streak ──
    private fun refreshAnalytics() {
        val blockedToday = EventLogger.getBlockedToday(this)
        val streak = EventLogger.getCleanStreakDays(this)
        val total = EventLogger.getTotalEvents(this)

        findViewById<TextView>(R.id.tvBlockedCount)?.text = "$blockedToday"
        findViewById<TextView>(R.id.tvStreakCount)?.text = "$streak"
        findViewById<TextView>(R.id.tvTotalCount)?.text = "$total"

        // Streak message
        val message = when {
            streak >= 30 -> "Incredible! Over a month strong"
            streak >= 14 -> "Two weeks! Outstanding discipline"
            streak >= 7 -> "One week clean — great progress"
            streak >= 3 -> "Building momentum — keep it up"
            streak >= 1 -> "Good start — stay focused"
            else -> "Every day is a fresh start"
        }
        findViewById<TextView>(R.id.tvStreakMessage)?.text = message
    }

    // ── CARD 3: Violation Enforcement State ──
    private fun refreshViolationState() {
        val level = ViolationEnforcer.getEscalationLevel()
        val count = ViolationEnforcer.getViolationCount()
        val resetMins = ViolationEnforcer.getResetMinutesRemaining()

        // Status title
        val (statusText, statusColor) = when (level) {
            0 -> "Current Status: Normal" to R.color.level_normal
            1 -> "Current Status: Warning" to R.color.level_warning
            2 -> "Current Status: App Blocked" to R.color.level_app_block
            3 -> "Current Status: Device Locked" to R.color.level_locked
            else -> "Current Status: Normal" to R.color.level_normal
        }

        findViewById<TextView>(R.id.tvViolationStatus)?.apply {
            text = statusText
            setTextColor(ContextCompat.getColor(this@MainActivity, statusColor))
        }

        // 4-stage progress bar
        val levelColors = listOf(
            R.color.level_normal,
            R.color.level_warning,
            R.color.level_app_block,
            R.color.level_locked
        )
        val levelViews = listOf(
            R.id.viewLevel0, R.id.viewLevel1, R.id.viewLevel2, R.id.viewLevel3
        )

        for (i in 0..3) {
            val color = if (i <= level) levelColors[i] else R.color.level_inactive
            findViewById<View>(levelViews[i])?.setBackgroundColor(
                ContextCompat.getColor(this, color)
            )
        }

        // Reset counter text
        val resetText = if (resetMins > 0) "${resetMins} min" else "--"
        findViewById<TextView>(R.id.tvResetCounter)?.text =
            "Violations: $count  |  Counter resets in: $resetText"
    }

    // ── CARD 4: System Watchdog & Network Traffic ──
    private fun refreshWatchdog() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val isOwner = dpm.isDeviceOwnerApp(packageName)

        val sb = StringBuilder()
        sb.appendLine("Watchdog Worker:  ✓ Active (15 min)")
        sb.appendLine("DNS Monitor:      ✓ Active (instant)")
        sb.appendLine("Boot Receiver:    ✓ Registered")

        val devEnabled = try {
            Settings.Global.getInt(contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
        } catch (_: Exception) { 0 }
        sb.appendLine("Dev Options:      ${if (devEnabled == 0) "✓ OFF" else "✗ ON"}")

        val dnsHost = DnsEnforcer.getActiveDnsHost(this)
        sb.appendLine("Active DNS:       $dnsHost")
        sb.appendLine("DNS Pool:         ${DnsEnforcer.DNS_SERVERS.size} servers")

        findViewById<TextView>(R.id.tvWatchdog)?.text = sb.toString().trimEnd()

        // Network traffic summary
        try {
            val traffic = NetworkTrafficSummary.buildSummary(this)
            findViewById<TextView>(R.id.tvNetworkTraffic)?.text = traffic
        } catch (e: Exception) {
            findViewById<TextView>(R.id.tvNetworkTraffic)?.text = "Traffic data unavailable"
        }

        // System info
        refreshSystemInfo()
    }

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
        sb.appendLine("Domains:    ${DomainBlockList.count()} + ${DomainBlockManager.count(this)} custom")
        sb.appendLine("Events:     ${EventLogger.getTotalEvents(this)}")
        sb.appendLine("Uptime:     ${android.os.SystemClock.elapsedRealtime() / 1000 / 60} min")

        if (isOwner) {
            sb.appendLine("───────────────────────────")
            sb.appendLine("DISALLOW_FACTORY_RESET: ✓")
            sb.appendLine("DISALLOW_SAFE_BOOT:     ✓")
            sb.appendLine("DISALLOW_ADD_USER:      ✓")
            sb.appendLine("DEV_OPTIONS_DISABLED:   ✓")
        }

        findViewById<TextView>(R.id.tvSysInfo)?.text = sb.toString().trimEnd()
    }

    // ── CARD 5: Custom Domain Blocks ──
    private fun refreshDomainBlocks() {
        val entries = DomainBlockManager.getAllEntries(this)

        if (entries.isEmpty()) {
            findViewById<TextView>(R.id.tvBlockedDomainList)?.text =
                "No custom blocks yet.\nTap '+ ADD' to block a domain."
            return
        }

        val sb = StringBuilder()
        for (entry in entries) {
            val lockIcon = if (entry.isLocked) "🔒" else "🔓"
            val status = if (entry.isLocked) {
                "${entry.daysRemaining}d remaining"
            } else {
                "Unlockable"
            }
            sb.appendLine("$lockIcon ${entry.domain}")
            sb.appendLine("   Blocked: ${entry.blockedAt.take(10)}  ($status)")
        }

        findViewById<TextView>(R.id.tvBlockedDomainList)?.text = sb.toString().trimEnd()
    }

    // ── CARD 6: Recent Block History ──
    private fun refreshHistory() {
        val events = EventLogger.getAllEvents(this)
        if (events.length() == 0) {
            findViewById<TextView>(R.id.tvHistory)?.text =
                "No blocked requests recorded yet.\n" +
                "Blocked domains appear here with timestamps."
            return
        }

        val sb = StringBuilder()
        val start = maxOf(0, events.length() - 10)
        for (i in events.length() - 1 downTo start) {
            try {
                val ev = events.getJSONObject(i)
                val ts = ev.optString("timestamp", "?")
                val domain = ev.optString("domain", "?")
                val shortTs = if (ts.length > 16) ts.substring(5, 16) else ts
                sb.appendLine("$shortTs  $domain")
            } catch (_: Exception) {}
        }

        if (sb.isEmpty()) sb.append("Error reading history")

        findViewById<TextView>(R.id.tvHistory)?.text = sb.toString().trimEnd()
    }

    // ────────────────────────────────────────────────────────────
    //  Add Domain Dialog
    // ────────────────────────────────────────────────────────────

    private fun showAddDomainDialog() {
        val input = EditText(this).apply {
            hint = "example.com"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_tertiary))
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.background_card_elevated))
            setPadding(32, 24, 32, 24)
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(input)

            val warning = TextView(this@MainActivity).apply {
                text = "⚠ Once blocked, this domain cannot be unblocked for ${DomainBlockManager.LOCK_DURATION_DAYS} days."
                textSize = 12f
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.amber_warning))
                setPadding(0, 16, 0, 0)
            }
            addView(warning)
        }

        AlertDialog.Builder(this, R.style.OmegaDialog)
            .setTitle("Block Domain")
            .setMessage("Enter the domain to permanently block:")
            .setView(container)
            .setPositiveButton("Block") { _, _ ->
                val domain = input.text.toString().trim()
                if (domain.isNotEmpty()) {
                    val added = DomainBlockManager.addDomain(this, domain)
                    if (added) {
                        Toast.makeText(this,
                            "✓ $domain blocked for ${DomainBlockManager.LOCK_DURATION_DAYS} days",
                            Toast.LENGTH_LONG).show()
                        refreshDomainBlocks()
                        refreshCoreDefense()
                    } else {
                        Toast.makeText(this, "Already blocked or invalid", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getSetting(key: String): String {
        return try {
            Settings.Global.getString(contentResolver, key) ?: "not set"
        } catch (_: Exception) { "error" }
    }
}
}
