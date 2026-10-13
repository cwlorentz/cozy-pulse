package com.cozypulse.app

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

/**
 * Reads the heart rate your watch app (Samsung Health, Fitbit, Pixel Watch, etc.)
 * has written into Health Connect.
 */
class HealthRepo(private val context: Context) {

    val permissions = setOf(HealthPermission.getReadPermission(HeartRateRecord::class))

    fun sdkStatus(): Int = HealthConnectClient.getSdkStatus(context)

    private val client: HealthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    suspend fun hasPermission(): Boolean {
        if (sdkStatus() != HealthConnectClient.SDK_AVAILABLE) return false
        return client.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    suspend fun readHeartRate(startMs: Long, endMs: Long): List<HrSample> {
        val start = Instant.ofEpochMilli(startMs)
        val end = Instant.ofEpochMilli(endMs)
        val out = mutableListOf<HrSample>()
        var pageToken: String? = null
        do {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                    pageToken = pageToken,
                )
            )
            for (record in response.records) {
                for (sample in record.samples) {
                    val t = sample.time.toEpochMilli()
                    if (t in startMs..endMs) out += HrSample(t, sample.beatsPerMinute.toInt())
                }
            }
            pageToken = response.pageToken
        } while (pageToken != null)
        return out.distinctBy { it.time }.sortedBy { it.time }
    }
}
