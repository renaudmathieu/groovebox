package com.renaudmathieu

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class MusicPlayerRepository(
    private val client: HttpClient = HttpClient()
) {
    private fun platformParam(): String {
        val lower = getPlatform().name.lowercase()
        return when {
            "android" in lower -> "android"
            "ios" in lower -> "ios"
            else -> "jvm"
        }
    }

    private fun baseUrl(): String {
        val isAndroid = getPlatform().name.lowercase().contains("android")
        val host = if (isAndroid) "10.0.2.2" else "127.0.0.1"
        return "http://$host:$SERVER_PORT"
    }

    fun streamingUrlFor(trackId: String): String =
        "${baseUrl()}/track/$trackId?platform=${platformParam()}"

    suspend fun fetchTracksRaw(): String =
        client.get("${baseUrl()}/tracks").bodyAsText()

    fun parseFirstTrack(raw: String): Track? {
        // Expecting a single-element array with object shape:
        // [{"id":"...","title":"...","artist":"...","duration":12345}]
        val objRegex = Regex("\\{\\\"id\\\":\\\"([^\\\"]+)\\\",\\\"title\\\":\\\"([^\\\"]+)\\\",\\\"artist\\\":\\\"([^\\\"]+)\\\",\\\"duration\\\":(\\d+)\\}")
        val match = objRegex.find(raw) ?: return null
        val (id, title, artist, durationStr) = match.destructured
        val duration = durationStr.toLongOrNull() ?: 0L
        val url = streamingUrlFor(id)
        return Track(
            id = id,
            title = title,
            artist = artist,
            url = url,
            duration = duration
        )
    }

    suspend fun loadFirstTrack(): Track? {
        val raw = fetchTracksRaw()
        return parseFirstTrack(raw)
    }
}
