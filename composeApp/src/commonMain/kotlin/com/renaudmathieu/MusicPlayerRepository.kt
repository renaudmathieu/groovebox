package com.renaudmathieu

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class MusicPlayerRepository(
    private val audioPlayer: AudioPlayer,
    private val client: HttpClient = HttpClient()
) {
    private val json = Json { ignoreUnknownKeys = true }

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

    fun parseFirstTrack(raw: String): Track? =
        parseTracks(raw).firstOrNull()

    fun parseTracks(raw: String): List<Track> {
        // Parse JSON using kotlinx.serialization to be robust to field order and extras
        val tracks: List<Track> = json.decodeFromString(raw)
        // Ensure the streaming URL includes the platform query param and absolute base URL
        return tracks.map { t: Track ->
            t.copy(url = streamingUrlFor(t.id))
        }
    }

    suspend fun loadFirstTrack(): Track? {
        val raw = fetchTracksRaw()
        return parseFirstTrack(raw)
    }

    suspend fun loadTracks(): List<Track> {
        val raw = fetchTracksRaw()
        return parseTracks(raw)
    }

    // Audio control methods delegated to the underlying AudioPlayer
    fun play() = audioPlayer.play()
    fun pause() = audioPlayer.pause()
    fun stop() = audioPlayer.stop()
    fun seekTo(positionMs: Long) = audioPlayer.seekTo(positionMs)
    fun getCurrentPosition(): Long = audioPlayer.getCurrentPosition()
    fun getDuration(): Long = audioPlayer.getDuration()
    fun setDataSource(track: Track) = audioPlayer.setDataSource(track)
    fun release() = audioPlayer.release()
}
