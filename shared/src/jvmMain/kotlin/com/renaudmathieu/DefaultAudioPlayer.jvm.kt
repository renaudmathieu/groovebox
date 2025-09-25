package com.renaudmathieu

import javazoom.jlgui.basicplayer.BasicController
import javazoom.jlgui.basicplayer.BasicPlayer
import javazoom.jlgui.basicplayer.BasicPlayerEvent
import javazoom.jlgui.basicplayer.BasicPlayerListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.net.URL
import kotlin.coroutines.resume

class DefaultAudioPlayer : AudioPlayer {

    private val basicPlayer = BasicPlayer()
    private val _position = MutableStateFlow(0L)
    private var positionUpdateJob: Job? = null
    private var trackDuration: Long = 0L
    private var isPlayerReady = false

    override val position: StateFlow<Long> = _position.asStateFlow()

    override val duration: Long
        get() = trackDuration

    override val currentVolume: Float
        get() = basicPlayer.gainValue

    private val playerListener = object : BasicPlayerListener {
        override fun opened(stream: Any?, properties: Map<*, *>?) {
            properties?.let { props ->
                val durationMicros = props["duration"] as? Long
                val durationMs = props["audio.length.frames"] as? Long
                val frameRate = props["audio.framerate.fps"] as? Float

                // Calculate duration in milliseconds
                trackDuration = when {
                    durationMicros != null -> durationMicros / 1000
                    durationMs != null && frameRate != null ->
                        ((durationMs / frameRate) * 1000).toLong()

                    else -> 0L
                }
            }
            isPlayerReady = true
        }

        override fun progress(
            bytesread: Int,
            microseconds: Long,
            pcmdata: ByteArray?,
            properties: Map<*, *>?
        ) {
            _position.value = microseconds / 1000
        }

        override fun stateUpdated(event: BasicPlayerEvent?) {
            event?.let {
                when (it.code) {
                    BasicPlayerEvent.PLAYING -> startPositionUpdates()
                    BasicPlayerEvent.PAUSED, BasicPlayerEvent.STOPPED -> stopPositionUpdates()
                }
            }
        }

        override fun setController(controller: BasicController?) {
            // No-op
        }

    }

    init {
        basicPlayer.addBasicPlayerListener(playerListener)
    }

    override suspend fun load(track: Track) = suspendCancellableCoroutine<Unit> { continuation ->
        try {
            isPlayerReady = false
            trackDuration = 0L
            _position.value = 0L

            val audioSource = when {
                track.url.startsWith("http://") || track.url.startsWith("https://") ->
                    URL(track.url)

                else -> File(track.url)
            }

            basicPlayer.open(URL(track.url))
            // Wait for the player to be ready
            val checkReadyJob = CoroutineScope(Dispatchers.IO).launch {
                var attempts = 0
                while (!isPlayerReady && attempts < 50) { // 5 second timeout
                    delay(100)
                    attempts++
                }

                if (isPlayerReady) {
                    continuation.resume(Unit)
                } else {
                    continuation.resume(Unit) // Resume anyway to avoid hanging
                }
            }

            continuation.invokeOnCancellation {
                checkReadyJob.cancel()
            }

        } catch (e: Exception) {
            continuation.resume(Unit) // Resume with empty state on error
        }
    }

    override suspend fun seekTo(positionMs: Long) {
        try {
            if (trackDuration > 0) {
                val seekRatio = positionMs / trackDuration
                basicPlayer.seek(seekRatio.coerceIn(0, 1))
            }
        } catch (e: Exception) {
            // Seek failed, ignore
        }
    }

    override suspend fun isMuted(): Boolean {
        return basicPlayer.gainValue == 0f
    }

    override fun play() {
        try {
            if (basicPlayer.status == BasicPlayer.PAUSED) {
                basicPlayer.resume()
            } else {
                basicPlayer.play()
            }
        } catch (e: Exception) {
            // Play failed, ignore
        }
    }

    override fun pause() {
        try {
            basicPlayer.pause()
        } catch (e: Exception) {
            // Pause failed, ignore
        }
    }

    override fun stop() {
        try {
            basicPlayer.stop()
            _position.value = 0L
            stopPositionUpdates()
        } catch (e: Exception) {
            // Stop failed, ignore
        }
    }

    override fun release() {
        try {
            stopPositionUpdates()
            basicPlayer.removeBasicPlayerListener(playerListener)
            basicPlayer.stop()
        } catch (e: Exception) {
            // Release failed, ignore
        }
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = CoroutineScope(Dispatchers.IO).launch {
            while (basicPlayer.status == BasicPlayer.PLAYING) {
                try {
                    val currentPos = trackDuration
                    _position.value = currentPos
                } catch (e: Exception) {
                    // Position update failed, ignore
                }
                delay(100) // Update every 100ms
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
}
