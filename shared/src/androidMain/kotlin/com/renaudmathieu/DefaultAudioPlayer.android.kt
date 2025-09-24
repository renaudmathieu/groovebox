package com.renaudmathieu

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume

class DefaultAudioPlayer : AudioPlayer, Player.Listener {

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(AppContextHolder.applicationContext).build()
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var tickerJob: Job? = null

    private val _position = MutableStateFlow(0L)
    override val position: StateFlow<Long> = _position.asStateFlow()

    private var _durationMs: Long = 0L
    override val duration: Long
        get() = _durationMs

    override val currentVolume: Float
        get() = player.volume

    override suspend fun isMuted(): Boolean = player.volume == 0f

    init {
        player.addListener(this)
    }

    override suspend fun load(track: Track) {
        _position.value = 0L
        _durationMs = 0L
        return suspendCancellableCoroutine { cont ->
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        player.removeListener(this)
                        val d = player.duration
                        _durationMs = if (d == C.TIME_UNSET) 0L else d
                        if (!cont.isCompleted) cont.resume(Unit)
                    }
                }
            }
            player.addListener(listener)
            val mediaItem = MediaItem.fromUri(track.url)
            player.setMediaItem(mediaItem)
            player.prepare()
            cont.invokeOnCancellation { player.removeListener(listener) }
        }
    }

    override fun play() {
        player.playWhenReady = true
        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun stop() {
        player.stop()
        player.clearMediaItems()
        _position.value = 0L
    }

    override fun release() {
        tickerJob?.cancel()
        player.removeListener(this)
        player.release()
        scope.cancel()
    }

    override suspend fun seekTo(positionMs: Long) {
        return suspendCancellableCoroutine { cont ->
            val listener = object : Player.Listener {
                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int
                ) {
                    if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                        player.removeListener(this)
                        if (!cont.isCompleted) cont.resume(Unit)
                    }
                }
            }
            player.addListener(listener)
            player.seekTo(positionMs)
            cont.invokeOnCancellation { player.removeListener(listener) }
        }
    }

    // Player.Listener callbacks
    override fun onPlaybackStateChanged(state: Int) {
        _position.value = player.currentPosition
        if (state == Player.STATE_READY) {
            val d = player.duration
            _durationMs = if (d == C.TIME_UNSET) 0L else d
        }
        updateTicker()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        updateTicker()
    }

    override fun onEvents(p: Player, events: Player.Events) {
        _position.value = p.currentPosition
        if (events.contains(Player.EVENT_TIMELINE_CHANGED) || events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) || events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
            val d = p.duration
            _durationMs = if (d == C.TIME_UNSET) 0L else d
        }
    }

    private fun updateTicker() {
        val shouldRun = player.isPlaying
        if (shouldRun && tickerJob == null) {
            tickerJob = scope.launch {
                while (player.isPlaying) {
                    _position.value = player.currentPosition
                    delay(100)
                }
                tickerJob = null
            }
        } else if (!shouldRun) {
            tickerJob?.cancel()
            tickerJob = null
        }
    }
}
