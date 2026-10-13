package com.cozypulse.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cozypulse.app.CozyViewModel
import com.cozypulse.app.Screen

@Composable
fun DetailScreen(vm: CozyViewModel, id: String) {
    val s = vm.sessions.find { it.id == id }
    if (s == null) {
        LaunchedEffect(id) { vm.screen = Screen.Home }
        return
    }
    val stats = s.stats
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(id) { vm.maybeAutoResync(id) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.screen = Screen.Home }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { vm.resync(id) }, enabled = !vm.syncing) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Sync again")
                }
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                }
            }

            Column {
                Text(s.title.ifBlank { "ASMR session" }, style = MaterialTheme.typography.headlineMedium)
                Text(
                    "${formatDay(s.startMs)} · ${formatDuration(s.durationMs)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftCocoa,
                )
            }

            if (stats == null) {
                CozyCard {
                    Text(
                        if (vm.syncing) "Gathering your heartbeats…" else "No heart rate here yet",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Your watch may not have synced this stretch to your phone yet. Open your watch's app " +
                            "(Samsung Health, Fitbit, etc.) to nudge a sync, then tap the refresh button above.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftCocoa,
                    )
                }
            } else {
                CozyCard {
                    Text(stats.mood, style = MaterialTheme.typography.headlineSmall, color = Rose)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        when {
                            stats.settle > 0 -> "Your heart slowed by ${stats.settle} bpm from start to finish."
                            stats.settle < 0 -> "Your heart picked up ${-stats.settle} bpm. Tingles can do that."
                            else -> "Your heart stayed right where it was, nice and even."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftCocoa,
                    )
                    Spacer(Modifier.height(14.dp))
                    HeartChart(s.samples, s.startMs, s.endMs, s.tingles)
                    if (s.tingles.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Lavender dots mark your ${s.tingles.size} tingle moment${if (s.tingles.size == 1) "" else "s"}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftCocoa,
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    StatTile("Average", "${stats.avg}", Modifier.weight(1f))
                    StatTile("Lowest", "${stats.min}", Modifier.weight(1f))
                    StatTile("Highest", "${stats.max}", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    StatTile("First fifth", "${stats.startAvg}", Modifier.weight(1f))
                    StatTile("Last fifth", "${stats.endAvg}", Modifier.weight(1f))
                    StatTile("Calmest at", formatClock(stats.calmestAt), Modifier.weight(1f), Lavender)
                }
                Text(
                    "${s.samples.size} readings from your watch · synced ${formatClock(s.lastSyncedMs)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftCocoa,
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this session?") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; vm.delete(id) }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Keep it") }
            },
            containerColor = Cream,
        )
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier, accent: Color = Rose) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Oat),
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 14.dp)) {
            Text(
                value,
                style = if (value.length > 4) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                color = accent,
                maxLines = 1,
            )
            Text(label, style = MaterialTheme.typography.bodyMedium, color = SoftCocoa, maxLines = 1)
        }
    }
}
