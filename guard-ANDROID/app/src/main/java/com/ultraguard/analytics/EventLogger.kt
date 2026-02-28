package com.ultraguard.analytics

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════
 *  LAYER 7 — RELAPSE ANALYTICS (Local Only)
 * ═══════════════════════════════════════════════════════════════
 *
 *  Logs all blocked events locally. No cloud sync. No content
 *  inspection. Only records metadata:
 *
 *  {
 *    "timestamp": "2026-02-28T10:41:00",
 *    "domain": "blocked-domain.com",
 *    "app": "com.android.chrome"
 *  }
 *
 *  Dashboard metrics:
 *  - Attempts today
 *  - Weekly trend
 *  - Streak counter (clean days)
 *  - Peak risk times
 */
object EventLogger {

    private const val TAG = "OmegaAnalytics"
    private const val PREFS_NAME = "omega_analytics"
    private const val KEY_EVENTS = "blocked_events"
    private const val KEY_STREAK_START = "streak_start"
    private const val KEY_LAST_VIOLATION_DATE = "last_violation_date"
    private const val MAX_EVENTS = 1000  // Rolling buffer

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Log a blocked domain access event.
     */
    fun log(context: Context, domain: String, sourceApp: String) {
        try {
            val prefs = getPrefs(context)
            val events = getEvents(prefs)

            val event = JSONObject().apply {
                put("timestamp", dateFormat.format(Date()))
                put("domain", domain)
                put("app", sourceApp)
            }

            events.put(event)

            // Keep only last MAX_EVENTS
            val trimmed = if (events.length() > MAX_EVENTS) {
                val newArray = JSONArray()
                for (i in events.length() - MAX_EVENTS until events.length()) {
                    newArray.put(events.getJSONObject(i))
                }
                newArray
            } else events

            prefs.edit()
                .putString(KEY_EVENTS, trimmed.toString())
                .putString(KEY_LAST_VIOLATION_DATE, dayFormat.format(Date()))
                .apply()

            Log.d(TAG, "Logged: $domain from $sourceApp")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to log event: ${e.message}")
        }
    }

    /**
     * Get the number of blocked attempts today.
     */
    fun getBlockedToday(context: Context): Int {
        val prefs = getPrefs(context)
        val events = getEvents(prefs)
        val today = dayFormat.format(Date())
        var count = 0

        for (i in 0 until events.length()) {
            try {
                val ts = events.getJSONObject(i).getString("timestamp")
                if (ts.startsWith(today)) count++
            } catch (_: Exception) {}
        }

        return count
    }

    /**
     * Get the clean streak in days.
     * A "clean day" is a day with zero blocked attempts.
     */
    fun getCleanStreakDays(context: Context): Int {
        val prefs = getPrefs(context)
        val lastViolation = prefs.getString(KEY_LAST_VIOLATION_DATE, null)

        if (lastViolation == null) {
            // No violations ever recorded — streak since install
            val streakStart = prefs.getString(KEY_STREAK_START, null)
            if (streakStart == null) {
                // First run — set streak start to today
                prefs.edit().putString(KEY_STREAK_START, dayFormat.format(Date())).apply()
                return 0
            }
            return try {
                val start = dayFormat.parse(streakStart)!!
                val diffMs = Date().time - start.time
                (diffMs / (1000 * 60 * 60 * 24)).toInt()
            } catch (_: Exception) { 0 }
        }

        return try {
            val lastDate = dayFormat.parse(lastViolation)!!
            val today = dayFormat.parse(dayFormat.format(Date()))!!
            val diffMs = today.time - lastDate.time
            val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
            // If violation was today, streak is 0
            if (diffDays == 0) 0 else diffDays - 1
        } catch (_: Exception) { 0 }
    }

    /**
     * Get the total number of logged events.
     */
    fun getTotalEvents(context: Context): Int {
        return getEvents(getPrefs(context)).length()
    }

    /**
     * Get all events as a JSON array (for export/display).
     */
    fun getAllEvents(context: Context): JSONArray {
        return getEvents(getPrefs(context))
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getEvents(prefs: SharedPreferences): JSONArray {
        val raw = prefs.getString(KEY_EVENTS, "[]") ?: "[]"
        return try {
            JSONArray(raw)
        } catch (_: Exception) {
            JSONArray()
        }
    }
}
