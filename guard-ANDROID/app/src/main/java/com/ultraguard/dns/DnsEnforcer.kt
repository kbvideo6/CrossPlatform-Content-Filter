package com.ultraguard.dns

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

/**
 * ═══════════════════════════════════════════════════════════════
 *  SYSTEM PRIVATE DNS ENFORCEMENT (Primary Defense)
 * ═══════════════════════════════════════════════════════════════
 *
 *  Forces the system-wide Private DNS to a family-safe DNS server.
 *  Maintains a prioritized fallback list so if the active server
 *  goes down, the app automatically switches to the next healthy one.
 *
 *  Requires Device Owner privileges.
 */
object DnsEnforcer {

    private const val TAG = "OmegaDNS"

    private const val PRIVATE_DNS_MODE = "private_dns_mode"
    private const val PRIVATE_DNS_SPECIFIER = "private_dns_specifier"

    private const val PREFS_NAME = "omega_dns"
    private const val KEY_ACTIVE_DNS = "active_dns_index"

    /**
     * Family-safe DNS servers in priority order.
     * All block adult content at the resolver level.
     *
     *  1. Cloudflare Family  — fast, blocks malware + adult
     *  2. CleanBrowsing Family — aggressive family filter
     *  3. AdGuard Family     — blocks adult + ads + trackers
     *  4. Quad9 Secured      — blocks malware (less strict but reliable fallback)
     */
    val DNS_SERVERS = listOf(
        "family.cloudflare-dns.com",
        "family-filter-dns.cleanbrowsing.org",
        "dns-family.adguard.com",
        "dns.quad9.net"
    )

    /** For backward compatibility — always points to the first server */
    const val FILTERED_DNS_HOSTNAME = "family.cloudflare-dns.com"

    // ────────────────────────────────────────────────────────────
    //  Active DNS management
    // ────────────────────────────────────────────────────────────

    /** Get the currently active DNS hostname from preferences. */
    fun getActiveDnsHost(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val index = prefs.getInt(KEY_ACTIVE_DNS, 0)
        return DNS_SERVERS.getOrElse(index) { DNS_SERVERS[0] }
    }

    /** Save the active DNS index. */
    private fun setActiveDnsIndex(context: Context, index: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_ACTIVE_DNS, index).apply()
    }

    // ────────────────────────────────────────────────────────────
    //  Enforcement
    // ────────────────────────────────────────────────────────────

    /**
     * Apply Private DNS settings using the currently active server.
     * Overload with Context for fallback-aware enforcement.
     */
    fun enforcePrivateDns(context: Context, dpm: DevicePolicyManager, admin: ComponentName) {
        val host = getActiveDnsHost(context)
        try {
            dpm.setGlobalSetting(admin, PRIVATE_DNS_MODE, "hostname")
            dpm.setGlobalSetting(admin, PRIVATE_DNS_SPECIFIER, host)
            Log.i(TAG, "Private DNS locked to $host")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enforce Private DNS: ${e.message}")
        }
    }

    /**
     * Legacy overload (no context) — uses first server in the list.
     * Kept for backward compat with callers that don't have a Context.
     */
    fun enforcePrivateDns(dpm: DevicePolicyManager, admin: ComponentName) {
        try {
            dpm.setGlobalSetting(admin, PRIVATE_DNS_MODE, "hostname")
            dpm.setGlobalSetting(admin, PRIVATE_DNS_SPECIFIER, FILTERED_DNS_HOSTNAME)
            Log.i(TAG, "Private DNS locked to $FILTERED_DNS_HOSTNAME (legacy)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enforce Private DNS: ${e.message}")
        }
    }

    /**
     * Re-apply DNS settings. Called by the watchdog periodically.
     */
    fun reapply(context: Context, dpm: DevicePolicyManager, admin: ComponentName) {
        enforcePrivateDns(context, dpm, admin)
    }

    // ────────────────────────────────────────────────────────────
    //  Health check & automatic fallback
    // ────────────────────────────────────────────────────────────

    /**
     * Check if internet is reachable (bypass DNS — connect to IP directly).
     * Uses Cloudflare's anycast IP on port 53 with a 3-second timeout.
     */
    fun isInternetAvailable(): Boolean {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress("1.1.1.1", 53), 3000)
            socket.close()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Internet check failed: ${e.message}")
            false
        }
    }

    /**
     * Check if a DNS-over-TLS server is reachable on port 853.
     * First resolves the hostname, then connects to port 853.
     */
    fun isDnsServerHealthy(hostname: String): Boolean {
        return try {
            // Step 1: Can we resolve the hostname?
            val addresses = InetAddress.getAllByName(hostname)
            if (addresses.isEmpty()) return false

            // Step 2: Can we connect to it on DoT port 853?
            val socket = Socket()
            socket.connect(InetSocketAddress(addresses[0], 853), 5000)
            socket.close()
            true
        } catch (e: Exception) {
            Log.d(TAG, "DNS health check failed for $hostname: ${e.message}")
            false
        }
    }

    /**
     * Check if DNS resolution is actually working end-to-end.
     * Tries to resolve a well-known domain.
     */
    fun isDnsResolutionWorking(): Boolean {
        return try {
            val addresses = InetAddress.getAllByName("www.google.com")
            addresses.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Main health-check + fallback logic. Called by WatchdogWorker.
     *
     * Logic:
     *  1. If internet is down → do nothing (not a DNS problem)
     *  2. If DNS resolution works → current server is fine
     *  3. If internet is up but DNS fails → DNS server is down
     *     → try each server in the fallback list
     *     → switch to the first healthy one
     */
    fun checkAndFallback(context: Context, dpm: DevicePolicyManager, admin: ComponentName) {
        // Run network checks on a background thread (WorkManager already runs on bg thread)
        if (!isInternetAvailable()) {
            Log.i(TAG, "No internet connectivity — skipping DNS health check")
            return
        }

        if (isDnsResolutionWorking()) {
            Log.i(TAG, "DNS resolution working — current server is healthy")
            return
        }

        // Internet works but DNS doesn't → DNS server problem
        val currentHost = getActiveDnsHost(context)
        Log.w(TAG, "DNS resolution FAILED with $currentHost — trying fallback servers")

        for ((index, server) in DNS_SERVERS.withIndex()) {
            if (server == currentHost) continue
            if (isDnsServerHealthy(server)) {
                Log.i(TAG, "Switching to healthy DNS server: $server (index=$index)")
                setActiveDnsIndex(context, index)
                enforcePrivateDns(context, dpm, admin)
                return
            }
            Log.d(TAG, "Fallback $server also unhealthy, trying next...")
        }

        Log.w(TAG, "ALL DNS servers appear unreachable — keeping current ($currentHost)")
    }
}
