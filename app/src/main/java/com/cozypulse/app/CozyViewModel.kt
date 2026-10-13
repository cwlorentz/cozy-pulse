package com.cozypulse.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface Screen {
    data object Home : Screen
    data object Active : Screen
    data class Detail(val id: String) : Screen
}

class CozyViewModel(app: Application) : AndroidViewModel(app) {
    val health = HealthRepo(app)
    private val store = SessionStore(app)

    var sdkStatus by mutableStateOf(HealthConnectClient.SDK_UNAVAILABLE)
        private set
    var hasPermission by mutableStateOf(false)
        private set
    var sessions by mutableStateOf(store.loadSessions())
        private set
    var active by mutableStateOf(store.loadActive())
        private set
    var liveSamples by mutableStateOf<List<HrSample>>(emptyList())
        private set
    var syncing by mutableStateOf(false)
        private set
    var screen by mutableStateOf<Screen>(if (store.loadActive() != null) Screen.Active else Screen.Home)

    fun refreshHealthStatus() {
        sdkStatus = health.sdkStatus()
        viewModelScope.launch {
            hasPermission = runCatching { health.hasPermission() }.getOrDefault(false)
        }
    }

    fun onPermissionResult(granted: Set<String>) {
        hasPermission = granted.containsAll(health.permissions)
    }

    fun start(title: String) {
        val a = ActiveSession(startMs = System.currentTimeMillis(), title = title.trim())
        active = a
        liveSamples = emptyList()
        store.saveActive(a)
        screen = Screen.Active
    }

    fun tingle() {
        val a = active ?: return
        val updated = a.copy(tingles = a.tingles + System.currentTimeMillis())
        active = updated
        store.saveActive(updated)
    }

    /** Called every few seconds while the session screen is open. */
    suspend fun pollLive() {
        val a = active ?: return
        if (!hasPermission) return
        runCatching { health.readHeartRate(a.startMs, System.currentTimeMillis()) }
            .onSuccess { liveSamples = it }
    }

    fun stop() {
        val a = active ?: return
        val end = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        viewModelScope.launch {
            syncing = true
            val samples = if (hasPermission) {
                runCatching { health.readHeartRate(a.startMs, end) }.getOrDefault(liveSamples)
            } else {
                liveSamples
            }
            val session = Session(
                id = id,
                startMs = a.startMs,
                endMs = end,
                title = a.title,
                tingles = a.tingles,
                samples = samples,
                lastSyncedMs = System.currentTimeMillis(),
            )
            sessions = listOf(session) + sessions
            store.saveSessions(sessions)
            active = null
            store.saveActive(null)
            liveSamples = emptyList()
            syncing = false
            screen = Screen.Detail(id)
        }
    }

    fun cancelActive() {
        active = null
        store.saveActive(null)
        liveSamples = emptyList()
        screen = Screen.Home
    }

    /** Watches often sync to the phone late, so re-read the session's window. */
    fun resync(id: String) {
        val s = sessions.find { it.id == id } ?: return
        if (!hasPermission) return
        viewModelScope.launch {
            syncing = true
            runCatching { health.readHeartRate(s.startMs, s.endMs) }.onSuccess { fresh ->
                val merged = (s.samples + fresh).distinctBy { it.time }.sortedBy { it.time }
                sessions = sessions.map {
                    if (it.id == id) it.copy(samples = merged, lastSyncedMs = System.currentTimeMillis()) else it
                }
                store.saveSessions(sessions)
            }
            syncing = false
        }
    }

    /** Re-sync automatically while a session is still young enough that the watch may not have caught up. */
    fun maybeAutoResync(id: String) {
        val s = sessions.find { it.id == id } ?: return
        val twoHours = 2 * 60 * 60 * 1000L
        if (s.samples.isEmpty() || s.lastSyncedMs - s.endMs < twoHours) resync(id)
    }

    fun delete(id: String) {
        sessions = sessions.filterNot { it.id == id }
        store.saveSessions(sessions)
        screen = Screen.Home
    }
}
