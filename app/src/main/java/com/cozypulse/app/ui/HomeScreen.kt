package com.cozypulse.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import com.cozypulse.app.CozyViewModel
import com.cozypulse.app.Screen
import com.cozypulse.app.Session

@Composable
fun HomeScreen(
    vm: CozyViewModel,
    onRequestPermission: () -> Unit,
    onInstallHealthConnect: () -> Unit,
    onOpenHealthConnect: () -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            Modifier.safeDrawingPadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(Modifier.padding(top = 12.dp, bottom = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Favorite, contentDescription = null, tint = Rose)
                        Spacer(Modifier.width(10.dp))
                        Text("Cozy Pulse", style = MaterialTheme.typography.displaySmall)
                    }
                    Text(
                        "Get comfy, press play, and see how your heart unwinds.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SoftCocoa,
                    )
                }
            }

            item {
                HealthCard(
                    sdkStatus = vm.sdkStatus,
                    hasPermission = vm.hasPermission,
                    onRequestPermission = onRequestPermission,
                    onInstall = onInstallHealthConnect,
                    onOpenSettings = onOpenHealthConnect,
                )
            }

            item {
                CozyCard {
                    Text("Start a session", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("What are you watching? (optional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Rose,
                            unfocusedBorderColor = Peach,
                            focusedContainerColor = Cream,
                            unfocusedContainerColor = Cream,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { vm.start(title); title = "" },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Begin", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            if (vm.sessions.isNotEmpty()) {
                item {
                    Text(
                        "Past sessions",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
                items(vm.sessions, key = { it.id }) { s ->
                    SessionRow(s) { vm.screen = Screen.Detail(s.id) }
                }
            } else {
                item {
                    Text(
                        "Your sessions will gather here like a little blanket pile.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftCocoa,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun CozyCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Oat),
    ) {
        Column(Modifier.padding(20.dp)) { content() }
    }
}

@Composable
private fun HealthCard(
    sdkStatus: Int,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onInstall: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    if (sdkStatus == HealthConnectClient.SDK_AVAILABLE && hasPermission) return
    CozyCard {
        when (sdkStatus) {
            HealthConnectClient.SDK_AVAILABLE -> {
                Text("Connect your watch", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Cozy Pulse reads heart rate from Health Connect, where your watch app " +
                        "(Samsung Health, Fitbit, Pixel Watch and others) saves it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftCocoa,
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRequestPermission, shape = RoundedCornerShape(50)) {
                        Text("Allow heart rate")
                    }
                    TextButton(onClick = onOpenSettings) { Text("Open Health Connect") }
                }
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                Text("Health Connect needs an update", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                Button(onClick = onInstall, shape = RoundedCornerShape(50)) { Text("Update Health Connect") }
            }
            else -> {
                Text("Install Health Connect", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Your watch's heart rate reaches this app through Google's Health Connect.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftCocoa,
                )
                Spacer(Modifier.height(10.dp))
                Button(onClick = onInstall, shape = RoundedCornerShape(50)) { Text("Get Health Connect") }
            }
        }
    }
}

@Composable
private fun SessionRow(s: Session, onClick: () -> Unit) {
    val stats = s.stats
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Oat),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        s.title.ifBlank { "ASMR session" },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )
                    Text(
                        "${formatDay(s.startMs)} · ${formatShortDuration(s.durationMs)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftCocoa,
                    )
                }
                if (stats != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${stats.avg}", style = MaterialTheme.typography.headlineSmall, color = Rose)
                        Text("avg bpm", style = MaterialTheme.typography.bodyMedium, color = SoftCocoa)
                    }
                }
            }
            if (s.samples.size > 1) {
                Spacer(Modifier.height(8.dp))
                HeartChart(s.samples, s.startMs, s.endMs, s.tingles, height = 44.dp, showLabels = false)
            }
        }
    }
}
