package com.cozypulse.app

import kotlin.math.roundToInt

/** One heart rate reading from the watch. [time] is epoch milliseconds. */
data class HrSample(val time: Long, val bpm: Int)

/** A session that is currently running. */
data class ActiveSession(
    val startMs: Long,
    val title: String,
    val tingles: List<Long> = emptyList(),
)

/** A finished session. Samples are re-read from Health Connect when the watch syncs late. */
data class Session(
    val id: String,
    val startMs: Long,
    val endMs: Long,
    val title: String,
    val tingles: List<Long>,
    val samples: List<HrSample>,
    val lastSyncedMs: Long,
) {
    val durationMs: Long get() = endMs - startMs
    val stats: SessionStats? get() = SessionStats.from(samples)
}

data class SessionStats(
    val avg: Int,
    val min: Int,
    val max: Int,
    val startAvg: Int,
    val endAvg: Int,
    val calmestAt: Long,
) {
    /** Positive when the heart slowed down over the session. */
    val settle: Int get() = startAvg - endAvg

    val mood: String
        get() = when {
            settle >= 5 -> "Deeply relaxed"
            settle >= 2 -> "Settling in"
            settle > -2 -> "Steady and calm"
            else -> "A little buzzy"
        }

    companion object {
        fun from(samples: List<HrSample>): SessionStats? {
            if (samples.isEmpty()) return null
            val sorted = samples.sortedBy { it.time }
            // Compare the first and last fifth of the session to see how it settled.
            val slice = (sorted.size / 5).coerceAtLeast(1)
            val first = sorted.take(slice)
            val last = sorted.takeLast(slice)
            val calmest = sorted.minBy { it.bpm }
            return SessionStats(
                avg = sorted.map { it.bpm }.average().roundToInt(),
                min = calmest.bpm,
                max = sorted.maxOf { it.bpm },
                startAvg = first.map { it.bpm }.average().roundToInt(),
                endAvg = last.map { it.bpm }.average().roundToInt(),
                calmestAt = calmest.time,
            )
        }
    }
}
