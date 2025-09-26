package com.renaudmathieu

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
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
import kotlin.coroutines.resume

class DefaultAudioPlayer(
    private val context: Context
) : AudioPlayer {

    private var exoPlayer: ExoPlayer? = null
    private val _position = MutableStateFlow(0L)
    private val handler = Handler(Looper.getMainLooper())
    private var positionUpdateJob: Job? = null

    override val position: StateFlow<Long> = _position.asStateFlow()

    override val duration: Long
        get() = exoPlayer?.duration?.takeIf { it != C.TIME_UNSET } ?: 0L

    override val currentVolume: Float
        get() = exoPlayer?.volume ?: 1.0f

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> {
                    stopPositionUpdates()
                }

                Player.STATE_BUFFERING -> {
                    // Nothing to do
                }

                Player.STATE_READY -> {
                    if (exoPlayer?.isPlaying == true) {
                        startPositionUpdates()
                    }
                }

                Player.STATE_ENDED -> {
                    stopPositionUpdates()
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                startPositionUpdates()
            } else {
                stopPositionUpdates()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            exoPlayer?.stop()
        }
    }

    init {
        initializePlayer()
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context)
                .setLooper(Looper.getMainLooper())
                .build()
                .apply {
                    addListener(playerListener)
                }
        }
    }

    override suspend fun load(track: Track) = suspendCancellableCoroutine { continuation ->
        val player = exoPlayer ?: run {
            initializePlayer()
            exoPlayer!!
        }

        if (!track.url.startsWith("http")) {
            val file = File(track.url)
            if (!file.exists()) {
                continuation.resume(Unit)
                return@suspendCancellableCoroutine
            }

            if (file.length() == 0L) {
                continuation.resume(Unit)
                return@suspendCancellableCoroutine
            }
        }

        try {
            val mediaItem = if (track.url.startsWith("http")) {
                MediaItem.fromUri(track.url)
            } else {
                MediaItem.fromUri("file://${track.url}")
            }

            player.setMediaItem(mediaItem)
            player.prepare()

            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        player.removeListener(this)
                        _position.value = 0L
                        continuation.resume(Unit)
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    player.removeListener(this)
                    continuation.resume(Unit)
                }
            }

            player.addListener(listener)

            continuation.invokeOnCancellation {
                player.removeListener(listener)
            }

        } catch (e: Exception) {
            continuation.resume(Unit)
        }
    }

    override suspend fun seekTo(positionMs: Long) {
        handler.post {
            exoPlayer?.seekTo(positionMs)
            _position.value = positionMs
        }
    }

    override suspend fun isMuted(): Boolean {
        return exoPlayer?.volume == 0f
    }

    override fun play() {
        handler.post {
            exoPlayer?.play()
        }
    }

    override fun pause() {
        handler.post {
            exoPlayer?.pause()
        }
    }

    override fun stop() {
        handler.post {
            exoPlayer?.stop()
            _position.value = 0L
            stopPositionUpdates()
        }
    }

    override fun release() {
        stopPositionUpdates()
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
        _position.value = 0L
    }

    private fun startPositionUpdates() {
        if (positionUpdateJob?.isActive == true) return

        positionUpdateJob = CoroutineScope(Dispatchers.Main).launch {
            while (exoPlayer?.isPlaying == true) {
                _position.value = exoPlayer?.currentPosition ?: 0L
                delay(100) // Update every 100ms
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
}