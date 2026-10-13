package com.cozypulse.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.cozypulse.app.CozyViewModel
import kotlinx.coroutines.delay

@Composable
fun ActiveScreen(vm: CozyViewModel) {
    val active = vm.active ?: return
    val lifecycleOwner = LocalLifecycleOwner.current
    var confirmDiscard by remember { mutableStateOf(false) }

    // Health Connect only answers while the app is on screen, so poll only then.
    LaunchedEffect(active.startMs) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                vm.pollLive()
                delay(15_000)
            }
        }
    }

    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1_000)
        }
    }

    val latest = vm.liveSamples.lastOrNull()
    // The heart breathes at your latest pace, or a slow 60 bpm while we wait.
    val beatMs = (60_000 / (latest?.bpm ?: 60)).coerceIn(300, 1500)
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(beatMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.safeDrawingPadding().padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                active.title.ifBlank { "Settling in" },
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(formatDuration(now - active.startMs), style = MaterialTheme.typography.titleMedium, color = SoftCocoa)

            Spacer(Modifier.weight(1f))

            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Peach.copy(alpha = 0.55f),
                    modifier = Modifier.size(220.dp).scale(pulse),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        latest?.bpm?.toString() ?: "…",
                        style = MaterialTheme.typography.displaySmall,
                        color = Cocoa,
                    )
                    Text("bpm", style = MaterialTheme.typography.bodyMedium, color = SoftCocoa)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                when {
                    !vm.hasPermission -> "Heart rate access isn't allowed yet, so this session will only keep its times."
                    latest == null -> "Waiting for your watch to sync. Watches often catch up later, and the full session fills in when you finish."
                    else -> "Last reading at ${formatClock(latest.time)}. Feel free to switch to your video; the session keeps going."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = SoftCocoa,
                textAlign = TextAlign.Center,
            )

            if (vm.liveSamples.size > 1) {
                Spacer(Modifier.height(12.dp))
                HeartChart(vm.liveSamples, active.startMs, now, active.tingles, height = 90.dp, showLabels = false)
            }

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = vm::tingle,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text(
                    if (active.tingles.isEmpty()) "✨ Tingles!" else "✨ Tingles! (${active.tingles.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = Cocoa,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { confirmDiscard = true }) { Text("Discard", color = SoftCocoa) }
                Button(
                    onClick = vm::stop,
                    enabled = !vm.syncing,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Rose),
                    modifier = Modifier.weight(1f).height(52.dp),
                ) {
                    Text(if (vm.syncing) "Gathering your heartbeats…" else "Finish session", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text("Discard this session?") },
            text = { Text("It won't be saved.") },
            confirmButton = {
                TextButton(onClick = { confirmDiscard = false; vm.cancelActive() }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDiscard = false }) { Text("Keep going") }
            },
            containerColor = Cream,
        )
    }
}
