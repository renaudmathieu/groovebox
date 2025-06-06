package com.renaudmathieu

import javazoom.jlgui.basicplayer.BasicController
import javazoom.jlgui.basicplayer.BasicPlayer
import javazoom.jlgui.basicplayer.BasicPlayerEvent
import javazoom.jlgui.basicplayer.BasicPlayerListener
import java.io.File
import java.net.URL
import kotlin.math.roundToLong

class DefaultAudioPlayer : AudioPlayer, BasicPlayerListener {

    private val player: BasicPlayer = BasicPlayer()
    private var _currentVolume: Float = 1.0f
    private var _isMuted: Boolean = false
    private var currentTrackDuration: Long = 0L // Store duration in milliseconds
    private var isPlayerReady: Boolean = false
    private var isSeeking = false

    init {
        player.addBasicPlayerListener(this)
    }

    override val currentVolume: Float get() = _currentVolume
    override val isMuted: Boolean get() = _isMuted

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
        isPlayerReady = false // Player needs to be re-opened after stop
    }

    override fun resume() {
        try {
            if (player.status == BasicPlayer.PAUSED) {
                player.resume()
            }
        } catch (e: Exception) {
            Logger.e("Error during resume: ${e.message}", e, tag = "DefaultAudioPlayer")
        }
    }

    override fun seekTo(positionMs: Long) {
        if (!isPlayerReady || currentTrackDuration == 0L) return
        try {
            isSeeking = true
            // BasicPlayer's seek is in microseconds
            player.seek(positionMs * 1000)
        } catch (e: Exception) {
            Logger.e("Error during seekTo: ${e.message}", e, tag = "DefaultAudioPlayer")
        } finally {
            // It's good practice to ensure seeking flag is reset
            // BasicPlayer might take some time to actually seek, this is a simplification
            // For more accurate seeking state, one might need to listen to PROGRESS events
            // and compare positions.
            isSeeking = false
        }
    }

    override fun getCurrentPosition(): Long {
        return lastKnownPositionMs
    }


    override fun getDuration(): Long {
        return currentTrackDuration
    }

    override fun setDataSource(track: Track) {
        try {
            if (player.status != BasicPlayer.STOPPED && player.status != BasicPlayer.UNKNOWN) {
                player.stop() // Stop any current playback
            }
            isPlayerReady = false
            currentTrackDuration = 0L

            if (track.url.startsWith("http://") || track.url.startsWith("https://")) {
                player.open(URL(track.url))
            } else {
                player.open(File(track.url))
            }
            // Note: `open` is synchronous and might block. For network streams,
            // it's better to do this in a background thread, then call play.
            // For simplicity here, we'll assume it's handled.
            // The `opened` callback will set `isPlayerReady` and `currentTrackDuration`.
        } catch (e: Exception) {
            Logger.e("Error setting data source: ${e.message}", e, tag = "DefaultAudioPlayer")
            isPlayerReady = false
        }
    }

    override fun reset() {
        try {
            if (player.status != BasicPlayer.STOPPED && player.status != BasicPlayer.UNKNOWN) {
                player.stop()
            }
        } catch (e: Exception) {
            Logger.e("Error during reset: ${e.message}", e, tag = "DefaultAudioPlayer")
        }
        isPlayerReady = false
        currentTrackDuration = 0L
        // BasicPlayer doesn't have a specific reset method like MediaPlayer.
        // Stopping it and re-opening a new source is the way.
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
        if (properties != null) {
            // Duration is often in microseconds in "duration" key
            val durationMicroseconds = properties["duration"] as? Long
            if (durationMicroseconds != null) {
                currentTrackDuration = (durationMicroseconds / 1000.0).roundToLong()
            } else {
                // Fallback for MP3, TDA files, etc.
                val mp3duration = properties["mp3.length.microseconds"] as? Long // Common for MP3
                if (mp3duration != null) {
                    currentTrackDuration = (mp3duration / 1000.0).roundToLong()
                } else {
                    Logger.w("Duration not found in properties: $properties", tag = "DefaultAudioPlayer")
                }
            }
            Logger.i(
                "Track opened. Duration: $currentTrackDuration ms. Properties: $properties",
                tag = "DefaultAudioPlayer"
            )
        } else {
            Logger.w("Track opened, but properties are null.", tag = "DefaultAudioPlayer")
        }

        // Apply initial volume
        setVolumeInternal(_currentVolume)
    }

    private var lastKnownPositionMs: Long = 0L

    override fun progress(
        bytesread: Int,
        microseconds: Long,
        pcmdata: ByteArray?,
        properties: MutableMap<Any?, Any?>?
    ) {
        if (!isSeeking) {
            lastKnownPositionMs = (microseconds / 1000.0).roundToLong()
        }
        // properties might contain current bitrate, etc.
        // This callback is useful for updating UI with playback progress.
        // The `getCurrentPosition` will now use `lastKnownPositionMs`
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
                }

                BasicPlayerEvent.EOM -> {
                    Logger.i("End of media reached.", tag = "DefaultAudioPlayer")
                    lastKnownPositionMs = currentTrackDuration // Or 0, depending on desired behavior
                }

                BasicPlayerEvent.SEEKED -> {
                    isSeeking = false // Seeking is complete
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
