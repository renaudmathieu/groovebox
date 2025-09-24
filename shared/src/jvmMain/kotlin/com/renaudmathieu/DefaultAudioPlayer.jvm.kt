package com.renaudmathieu

import javazoom.jlgui.basicplayer.BasicController
import javazoom.jlgui.basicplayer.BasicPlayer
import javazoom.jlgui.basicplayer.BasicPlayerEvent
import javazoom.jlgui.basicplayer.BasicPlayerListener
import java.io.File
import java.net.URL
import kotlin.math.roundToLong
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultAudioPlayer : AudioPlayer, BasicPlayerListener {

    private val player: BasicPlayer = BasicPlayer()
    private var _currentVolume: Float = 1.0f
    private var _isMuted: Boolean = false
    private var currentTrackDuration: Long = 0L // Store duration in milliseconds
    private var isPlayerReady: Boolean = false
    private var isSeeking = false

    private val _position = MutableStateFlow(0L)
    override val position: StateFlow<Long> = _position.asStateFlow()

    private var _durationMs: Long = 0L
    override val duration: Long
        get() = _durationMs

    private var loadDeferred: CompletableDeferred<Unit>? = null
    private var seekDeferred: CompletableDeferred<Unit>? = null

    init {
        player.addBasicPlayerListener(this)
    }

    override val currentVolume: Float get() = _currentVolume
    override suspend fun isMuted(): Boolean = _isMuted

    override fun play() {
        try {
            if (isPlayerReady && player.status != BasicPlayer.PLAYING) {
                player.play()
            } else if (!isPlayerReady) {
                Logger.w("Player not ready. Call setDataSource first.", tag = "DefaultAudioPlayer")
            }
        } catch (e: Exception) {
            Logger.e("Error during play: ${e.message}", e, tag = "DefaultAudioPlayer")
        }
    }

    override fun pause() {
        try {
            if (player.status == BasicPlayer.PLAYING) {
                player.pause()
            }
        } catch (e: Exception) {
            Logger.e("Error during pause: ${e.message}", e, tag = "DefaultAudioPlayer")
        }
    }

    override fun stop() {
        try {
            if (player.status == BasicPlayer.PLAYING || player.status == BasicPlayer.PAUSED) {
                player.stop()
            }
        } catch (e: Exception) {
            Logger.e("Error during stop: ${e.message}", e, tag = "DefaultAudioPlayer")
        }
        lastKnownPositionMs = 0L
        _position.value = 0L
        isPlayerReady = false // Player needs to be re-opened after stop
    }


    override suspend fun seekTo(positionMs: Long) {
        if (!isPlayerReady || currentTrackDuration == 0L) return
        val deferred = CompletableDeferred<Unit>()
        seekDeferred = deferred
        try {
            isSeeking = true
            // BasicPlayer's seek is in microseconds
            player.seek(positionMs * 1000)
            try {
                deferred.await()
            } finally {
                if (seekDeferred === deferred) seekDeferred = null
            }
        } catch (e: Exception) {
            Logger.e("Error during seekTo: ${e.message}", e, tag = "DefaultAudioPlayer")
        }
    }


    override suspend fun load(track: Track) {
        try {
            if (player.status != BasicPlayer.STOPPED && player.status != BasicPlayer.UNKNOWN) {
                player.stop() // Stop any current playback
            }
            isPlayerReady = false
            currentTrackDuration = 0L
            lastKnownPositionMs = 0L
            _position.value = 0L
            _durationMs = 0L

            // Prepare to await 'opened' callback
            val deferred = CompletableDeferred<Unit>()
            loadDeferred = deferred

            if (track.url.startsWith("http://") || track.url.startsWith("https://")) {
                player.open(URL(track.url))
            } else {
                player.open(File(track.url))
            }

            // Await until opened() signals readiness
            try {
                deferred.await()
            } finally {
                if (loadDeferred === deferred) loadDeferred = null
            }
        } catch (e: Exception) {
            Logger.e("Error setting data source: ${e.message}", e, tag = "DefaultAudioPlayer")
            isPlayerReady = false
        }
    }


    override fun release() {
        try {
            player.stop() // Ensure player is stopped
        } catch (e: Exception) {
            Logger.e("Error stopping player during release: ${e.message}", e, tag = "DefaultAudioPlayer")
        }
        // BasicPlayer doesn't have a dedicated release method.
        // Listeners are typically removed if the player instance is going to be garbage collected.
        // For this class, as `player` is a member, it will be GC'd with the class instance.
        // If BasicPlayer held system resources that needed explicit release beyond `stop()`,
        // the library's documentation would specify it.
        isPlayerReady = false
    }

    // --- BasicPlayerListener Implementation ---

    override fun opened(stream: Any?, properties: MutableMap<Any?, Any?>?) {
        isPlayerReady = true
        currentTrackDuration = 0L // Reset before trying to read
        lastKnownPositionMs = 0L
        _position.value = 0L
        if (properties != null) {
            // Duration is often in microseconds in one of several keys
            val durationUsCandidates = listOf(
                properties["duration"],
                properties["mp3.length.microseconds"],
                properties["audio.length.microseconds"]
            )
            val durationUs = durationUsCandidates.firstNotNullOfOrNull {
                when (it) {
                    is Long -> it
                    is Int -> it.toLong()
                    is Double -> it.roundToLong()
                    is Float -> it.toLong()
                    else -> null
                }
            }
            if (durationUs != null && durationUs > 0) {
                currentTrackDuration = (durationUs / 1000.0).roundToLong()
            } else {
                Logger.w("Duration not found in properties: $properties", tag = "DefaultAudioPlayer")
            }
            Logger.i(
                "Track opened. Duration: $currentTrackDuration ms. Properties: $properties",
                tag = "DefaultAudioPlayer"
            )
        } else {
            Logger.w("Track opened, but properties are null.", tag = "DefaultAudioPlayer")
        }

        // Push duration update
        _durationMs = currentTrackDuration
        // Apply initial volume
        setVolumeInternal(_currentVolume)
        // Complete any pending load() awaiter
        loadDeferred?.complete(Unit)
        loadDeferred = null
    }

    private var lastKnownPositionMs: Long = 0L

    override fun progress(
        bytesread: Int,
        microseconds: Long,
        pcmdata: ByteArray?,
        properties: MutableMap<Any?, Any?>?
    ) {
        if (!isSeeking) {
            val posUsFromArg = if (microseconds > 0) microseconds else null
            val posUsFromProps = when (val v = properties?.get("mp3.position.microseconds")) {
                is Long -> v
                is Int -> v.toLong()
                is Double -> v.roundToLong()
                is Float -> v.toLong()
                else -> null
            }
            val posUs = posUsFromArg ?: posUsFromProps
            if (posUs != null && posUs >= 0) {
                lastKnownPositionMs = (posUs / 1000.0).roundToLong()
                _position.value = lastKnownPositionMs
            }
        }
        // properties might contain current bitrate, etc.
    }

    // We need to override getCurrentPosition to use the value from `progress`
    // This is a common pattern when the player updates via events.
    fun getActualCurrentPosition(): Long {
        return lastKnownPositionMs
    }

    override fun stateUpdated(event: BasicPlayerEvent?) {
        event?.let {
            Logger.i("Player state updated: ${it.code} - ${it.description}", tag = "DefaultAudioPlayer")
            when (it.code) {
                BasicPlayerEvent.STOPPED -> {
                    // Reset position when stopped explicitly or at end of media
                    lastKnownPositionMs = 0L
                    _position.value = 0L
                }

                BasicPlayerEvent.EOM -> {
                    Logger.i("End of media reached.", tag = "DefaultAudioPlayer")
                    lastKnownPositionMs = currentTrackDuration // Or 0, depending on desired behavior
                    _position.value = lastKnownPositionMs
                }

                BasicPlayerEvent.SEEKED -> {
                    isSeeking = false // Seeking is complete
                    seekDeferred?.complete(Unit)
                    seekDeferred = null
                    // The `progress` event after seek will give the new position
                    Logger.i("Seek operation completed.", tag = "DefaultAudioPlayer")
                }

                BasicPlayerEvent.SEEKING -> {
                    isSeeking = true
                    Logger.i("Seek operation started.", tag = "DefaultAudioPlayer")
                }
                // Handle other states if necessary (PLAYING, PAUSED, GAIN, PAN)
            }
        }
    }

    override fun setController(controller: BasicController?) {
        // Not typically used by the application, but part of the listener interface.
    }

    // Helper for volume
    private fun setVolumeInternal(volume: Float) {
        try {
            // BasicPlayer volume is a gain value (0.0 to 1.0 typically, but can be > 1.0 for amplification)
            // It also has balance control (pan).
            // Let's assume volume maps directly to gain.
            player.setGain(if (_isMuted) 0.0 else volume.toDouble())
        } catch (e: Exception) {
            Logger.e("Error setting volume: ${e.message}", e, tag = "DefaultAudioPlayer")
        }
    }

    // Public methods from AudioPlayer that might affect volume
    fun setVolume(volume: Float) { // Assuming this is the intended API from a potential extended interface
        _currentVolume = volume.coerceIn(0.0f, 1.0f)
        setVolumeInternal(_currentVolume)
    }

    fun setMuted(muted: Boolean) { // Assuming this is the intended API
        _isMuted = muted
        setVolumeInternal(_currentVolume)
    }
}
