package com.ultraguard.analytics

import android.content.Context
import android.net.TrafficStats
import android.os.Build
import android.util.Log
import java.text.DecimalFormat

/**
 * ═══════════════════════════════════════════════════════════════
 *  NETWORK TRAFFIC SUMMARY
 * ═══════════════════════════════════════════════════════════════
 *
 *  Provides a summarized view of network traffic data using
 *  Android's TrafficStats API. Shows total data usage,
 *  active DNS info, and connection statistics.
 */
object NetworkTrafficSummary {

    private const val TAG = "OmegaTraffic"
    private val sizeFormat = DecimalFormat("#,##0.0")

    data class TrafficData(
        val totalRxBytes: Long,
        val totalTxBytes: Long,
        val mobileRxBytes: Long,
        val mobileTxBytes: Long,
        val totalRxFormatted: String,
        val totalTxFormatted: String,
        val mobileRxFormatted: String,
        val mobileTxFormatted: String
    )

    /**
     * Get current traffic statistics.
     */
    fun getTrafficData(): TrafficData {
        val totalRx = TrafficStats.getTotalRxBytes()
        val totalTx = TrafficStats.getTotalTxBytes()
        val mobileRx = TrafficStats.getMobileRxBytes()
        val mobileTx = TrafficStats.getMobileTxBytes()

        return TrafficData(
            totalRxBytes = totalRx,
            totalTxBytes = totalTx,
            mobileRxBytes = mobileRx,
            mobileTxBytes = mobileTx,
            totalRxFormatted = formatBytes(totalRx),
            totalTxFormatted = formatBytes(totalTx),
            mobileRxFormatted = formatBytes(mobileRx),
            mobileTxFormatted = formatBytes(mobileTx)
        )
    }

    /**
     * Build a formatted summary string for the dashboard.
     */
    fun buildSummary(context: Context): String {
        val data = getTrafficData()
        val sb = StringBuilder()

        sb.appendLine("Total Download:  ${data.totalRxFormatted}")
        sb.appendLine("Total Upload:    ${data.totalTxFormatted}")
        sb.appendLine("───────────────────────────")
        sb.appendLine("Mobile Down:     ${data.mobileRxFormatted}")
        sb.appendLine("Mobile Up:       ${data.mobileTxFormatted}")
        sb.appendLine("───────────────────────────")

        // WiFi estimate (total - mobile)
        val wifiRx = data.totalRxBytes - data.mobileRxBytes
        val wifiTx = data.totalTxBytes - data.mobileTxBytes
        if (wifiRx >= 0 && wifiTx >= 0) {
            sb.appendLine("Wi-Fi Down:      ${formatBytes(wifiRx)}")
            sb.appendLine("Wi-Fi Up:        ${formatBytes(wifiTx)}")
        }

        return sb.toString().trimEnd()
    }

    /**
     * Format bytes to human-readable string.
     */
    private fun formatBytes(bytes: Long): String {
        if (bytes < 0) return "N/A"
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${sizeFormat.format(bytes / 1024.0)} KB"
            bytes < 1024 * 1024 * 1024 -> "${sizeFormat.format(bytes / (1024.0 * 1024))} MB"
            else -> "${sizeFormat.format(bytes / (1024.0 * 1024 * 1024))} GB"
        }
    }
}
