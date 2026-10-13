package com.cozypulse.app.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDuration(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

fun formatShortDuration(ms: Long): String {
    val min = (ms / 60000).coerceAtLeast(0)
    return if (min >= 60) "${min / 60}h ${min % 60}m" else "${min}m"
}

fun formatDay(ms: Long): String = SimpleDateFormat("EEE, MMM d · h:mm a", Locale.getDefault()).format(Date(ms))

fun formatClock(ms: Long): String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ms))
