package com.cozypulse.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.cozypulse.app.ui.ActiveScreen
import com.cozypulse.app.ui.CozyTheme
import com.cozypulse.app.ui.DetailScreen
import com.cozypulse.app.ui.HomeScreen

class MainActivity : ComponentActivity() {
    private val vm: CozyViewModel by viewModels()
    private lateinit var permissionLauncher: ActivityResultLauncher<Set<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionLauncher = registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { granted -> vm.onPermissionResult(granted) }

        setContent {
            CozyTheme {
                when (val s = vm.screen) {
                    Screen.Home -> HomeScreen(
                        vm = vm,
                        onRequestPermission = { permissionLauncher.launch(vm.health.permissions) },
                        onInstallHealthConnect = ::openHealthConnectInStore,
                        onOpenHealthConnect = ::openHealthConnectSettings,
                    )
                    Screen.Active -> ActiveScreen(vm)
                    is Screen.Detail -> {
                        BackHandler { vm.screen = Screen.Home }
                        DetailScreen(vm, s.id)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.refreshHealthStatus()
    }

    private fun openHealthConnectInStore() {
        val pkg = "com.google.android.apps.healthdata"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setPackage("com.android.vending")
            data = Uri.parse("market://details?id=$pkg&url=healthconnect%3A%2F%2Fonboarding")
            putExtra("overlay", true)
            putExtra("callerId", packageName)
        }
        runCatching { startActivity(intent) }.onFailure {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")))
        }
    }

    private fun openHealthConnectSettings() {
        runCatching { startActivity(Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)) }
    }
}
