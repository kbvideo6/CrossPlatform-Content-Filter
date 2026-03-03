package com.ultraguard.dns

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * ═══════════════════════════════════════════════════════════════
 *  CUSTOM DOMAIN BLOCK MANAGER — 21-day lock
 * ═══════════════════════════════════════════════════════════════
 *
 *  Allows adding custom domains to the block list.
 *  Once blocked, a domain CANNOT be unblocked for 21 days.
 *  After 21 days, the domain can be removed (but must be done manually).
 *
 *  Storage: SharedPreferences as a JSON array of blocked entries.
 *  Each entry: { domain, blockedAt (timestamp), permanent (optional) }
 */
object DomainBlockManager {

    private const val TAG = "OmegaDomainBlock"
    private const val PREFS_NAME = "omega_domain_blocks"
    private const val KEY_BLOCKED = "blocked_domains"

    /** Number of days a domain must stay blocked before it can be removed */
    const val LOCK_DURATION_DAYS = 21

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

    // ────────────────────────────────────────────────────────────
    //  Add / Remove
    // ────────────────────────────────────────────────────────────

    /**
     * Add a domain to the custom block list.
     * Returns true if added, false if already blocked.
     */
    fun addDomain(context: Context, domain: String): Boolean {
        val normalized = domain.lowercase().trim().trimEnd('.')
        if (normalized.isBlank() || normalized.length < 4) return false

        val entries = getEntries(context)

        // Check if already blocked
        for (i in 0 until entries.length()) {
            val entry = entries.getJSONObject(i)
            if (entry.getString("domain") == normalized) {
                Log.d(TAG, "Domain already blocked: $normalized")
                return false
            }
        }

        val entry = JSONObject().apply {
            put("domain", normalized)
            put("blockedAt", dateFormat.format(Date()))
            put("blockedAtMillis", System.currentTimeMillis())
        }
        entries.put(entry)
        saveEntries(context, entries)

        Log.i(TAG, "Domain blocked: $normalized (locked for $LOCK_DURATION_DAYS days)")
        return true
    }

    /**
     * Remove a domain from the block list.
     * Only succeeds if the 21-day lock period has passed.
     * Returns: 0 = removed, -1 = not found, positive = days remaining
     */
    fun removeDomain(context: Context, domain: String): Int {
        val normalized = domain.lowercase().trim().trimEnd('.')
        val entries = getEntries(context)

        for (i in 0 until entries.length()) {
            val entry = entries.getJSONObject(i)
            if (entry.getString("domain") == normalized) {
                val remaining = getDaysRemaining(entry)
                if (remaining > 0) {
                    Log.w(TAG, "Cannot remove $normalized — $remaining days remaining")
                    return remaining
                }

                // Remove entry
                val newEntries = JSONArray()
                for (j in 0 until entries.length()) {
                    if (j != i) newEntries.put(entries.getJSONObject(j))
                }
                saveEntries(context, newEntries)
                Log.i(TAG, "Domain unblocked: $normalized")
                return 0
            }
        }
        return -1
    }

    // ────────────────────────────────────────────────────────────
    //  Query
    // ────────────────────────────────────────────────────────────

    /**
     * Check if a domain is in the custom block list.
     */
    fun isBlocked(context: Context, domain: String): Boolean {
        val normalized = domain.lowercase().trim().trimEnd('.')
        val entries = getEntries(context)
        for (i in 0 until entries.length()) {
            val entry = entries.getJSONObject(i)
            val blocked = entry.getString("domain")
            if (blocked == normalized) return true
            // Also check if it's a subdomain match
            if (normalized.endsWith(".$blocked")) return true
        }
        return false
    }

    /**
     * Get all blocked domains with their status info.
     * Returns a list of data objects for display.
     */
    data class BlockedEntry(
        val domain: String,
        val blockedAt: String,
        val daysRemaining: Int,
        val isLocked: Boolean
    )

    fun getAllEntries(context: Context): List<BlockedEntry> {
        val entries = getEntries(context)
        val result = mutableListOf<BlockedEntry>()

        for (i in 0 until entries.length()) {
            try {
                val entry = entries.getJSONObject(i)
                val domain = entry.getString("domain")
                val blockedAt = entry.optString("blockedAt", "unknown")
                val remaining = getDaysRemaining(entry)

                result.add(BlockedEntry(
                    domain = domain,
                    blockedAt = blockedAt,
                    daysRemaining = remaining,
                    isLocked = remaining > 0
                ))
            } catch (e: Exception) {
                Log.e(TAG, "Error reading entry $i: ${e.message}")
            }
        }
        return result
    }

    /**
     * Get the total count of custom blocked domains.
     */
    fun count(context: Context): Int = getEntries(context).length()

    /**
     * Get all blocked domain strings (for enforcement integration).
     */
    fun getBlockedDomains(context: Context): Set<String> {
        val entries = getEntries(context)
        val domains = mutableSetOf<String>()
        for (i in 0 until entries.length()) {
            try {
                domains.add(entries.getJSONObject(i).getString("domain"))
            } catch (_: Exception) {}
        }
        return domains
    }

    // ────────────────────────────────────────────────────────────
    //  Internal helpers
    // ────────────────────────────────────────────────────────────

    private fun getDaysRemaining(entry: JSONObject): Int {
        val blockedMillis = entry.optLong("blockedAtMillis", 0L)
        if (blockedMillis == 0L) return 0

        val elapsedMs = System.currentTimeMillis() - blockedMillis
        val elapsedDays = TimeUnit.MILLISECONDS.toDays(elapsedMs).toInt()
        return maxOf(0, LOCK_DURATION_DAYS - elapsedDays)
    }

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun getEntries(context: Context): JSONArray {
        val raw = getPrefs(context).getString(KEY_BLOCKED, "[]") ?: "[]"
        return try { JSONArray(raw) } catch (_: Exception) { JSONArray() }
    }

    private fun saveEntries(context: Context, entries: JSONArray) {
        getPrefs(context).edit().putString(KEY_BLOCKED, entries.toString()).apply()
    }
}
