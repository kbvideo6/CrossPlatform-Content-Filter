package com.ultraguard

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ultraguard.analytics.EventLogger

/**
 * Detailed block history view.
 * Shows all recorded blocked events with timestamps and source apps.
 * Accessed by tapping the Analytics & Streak card.
 */
class BlockHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_history)

        findViewById<TextView>(R.id.btnBack)?.setOnClickListener { finish() }

        loadHistory()
    }

    private fun loadHistory() {
        val events = EventLogger.getAllEvents(this)

        findViewById<TextView>(R.id.tvEventCount)?.text = "${events.length()} events"

        if (events.length() == 0) {
            findViewById<TextView>(R.id.tvFullHistory)?.text =
                "No blocked requests recorded yet.\n\n" +
                "When a blocked domain is accessed via DNS,\n" +
                "it will appear here with timestamp and source.\n\n" +
                "The DNS-level filter blocks adult content\n" +
                "before it ever reaches the device."
            return
        }

        val sb = StringBuilder()
        // Show ALL events, newest first
        for (i in events.length() - 1 downTo 0) {
            try {
                val ev = events.getJSONObject(i)
                val ts = ev.optString("timestamp", "?")
                val domain = ev.optString("domain", "?")
                val app = ev.optString("app", "?")

                sb.appendLine("╔════════════════════════════════════")
                sb.appendLine("║ $ts")
                sb.appendLine("║ Domain:  $domain")
                sb.appendLine("║ Source:  $app")
                sb.appendLine("╚════════════════════════════════════")
                sb.appendLine()
            } catch (_: Exception) {}
        }

        if (sb.isEmpty()) {
            sb.append("Error reading history")
        }

        findViewById<TextView>(R.id.tvFullHistory)?.text = sb.toString().trimEnd()
    }
}
