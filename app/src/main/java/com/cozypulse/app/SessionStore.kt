package com.cozypulse.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Keeps sessions in a small JSON file on the phone. Nothing leaves the device. */
class SessionStore(context: Context) {
    private val file = File(context.filesDir, "sessions.json")
    private val prefs = context.getSharedPreferences("active_session", Context.MODE_PRIVATE)

    fun loadSessions(): List<Session> {
        if (!file.exists()) return emptyList()
        return try {
            val arr = JSONArray(file.readText())
            (0 until arr.length()).map { sessionFromJson(arr.getJSONObject(it)) }
                .sortedByDescending { it.startMs }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveSessions(sessions: List<Session>) {
        val arr = JSONArray()
        sessions.forEach { arr.put(sessionToJson(it)) }
        val tmp = File(file.parentFile, "sessions.json.tmp")
        tmp.writeText(arr.toString())
        tmp.renameTo(file)
    }

    fun loadActive(): ActiveSession? {
        val start = prefs.getLong("start", 0L)
        if (start == 0L) return null
        val tingles = prefs.getString("tingles", "")!!
            .split(',').mapNotNull { it.toLongOrNull() }
        return ActiveSession(start, prefs.getString("title", "")!!, tingles)
    }

    fun saveActive(active: ActiveSession?) {
        prefs.edit().apply {
            if (active == null) {
                clear()
            } else {
                putLong("start", active.startMs)
                putString("title", active.title)
                putString("tingles", active.tingles.joinToString(","))
            }
        }.apply()
    }

    private fun sessionToJson(s: Session) = JSONObject().apply {
        put("id", s.id)
        put("start", s.startMs)
        put("end", s.endMs)
        put("title", s.title)
        put("synced", s.lastSyncedMs)
        put("tingles", JSONArray().apply { s.tingles.forEach { put(it) } })
        // Stored as flat [time, bpm, time, bpm, ...] to keep the file small.
        put("samples", JSONArray().apply {
            s.samples.forEach { put(it.time); put(it.bpm) }
        })
    }

    private fun sessionFromJson(o: JSONObject): Session {
        val t = o.optJSONArray("tingles") ?: JSONArray()
        val sa = o.optJSONArray("samples") ?: JSONArray()
        val samples = (0 until sa.length() / 2).map {
            HrSample(sa.getLong(it * 2), sa.getInt(it * 2 + 1))
        }
        return Session(
            id = o.getString("id"),
            startMs = o.getLong("start"),
            endMs = o.getLong("end"),
            title = o.optString("title", ""),
            tingles = (0 until t.length()).map { t.getLong(it) },
            samples = samples,
            lastSyncedMs = o.optLong("synced", 0L),
        )
    }
}
