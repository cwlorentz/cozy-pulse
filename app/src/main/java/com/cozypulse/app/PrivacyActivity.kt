package com.cozypulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cozypulse.app.ui.CozyTheme

/** Shown by Health Connect when you ask why Cozy Pulse wants heart rate access. */
class PrivacyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CozyTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        Modifier.padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text("Why Cozy Pulse reads your heart rate", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Cozy Pulse reads only the heart rate your watch saved during sessions you start " +
                                "yourself, so it can show how your body relaxes while you watch ASMR. " +
                                "Everything stays on this phone. Nothing is uploaded or shared.",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Button(onClick = { finish() }) { Text("Got it") }
                    }
                }
            }
        }
    }
}
