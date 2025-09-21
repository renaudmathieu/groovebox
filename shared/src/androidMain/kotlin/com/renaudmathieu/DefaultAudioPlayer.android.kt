
package com.renaudmathieu

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class DefaultAudioPlayer : AudioPlayer {

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(AppContextHolder.applicationContext).build()
    }

    override val currentVolume: Float
        get() = player.volume

    override val isMuted: Boolean
        get() = player.volume == 0f

    // New API
    override fun load(track: Track) {
        val mediaItem = MediaItem.fromUri(track.url)
        player.setMediaItem(mediaItem)
        player.prepare()
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
    }

    override fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    override fun position(): Long {
        return player.currentPosition
    }

    override fun duration(): Long {
        val d = player.duration
        return if (d == C.TIME_UNSET) 0L else d
    }

    // Legacy API bridging (to satisfy existing callers)
    override fun getCurrentPosition(): Long = position()

    override fun getDuration(): Long = duration()

    override fun setDataSource(track: Track) = load(track)

    override fun reset() = stop()

    override fun resume() = play()

    override fun release() {
        player.release()
    }
}
