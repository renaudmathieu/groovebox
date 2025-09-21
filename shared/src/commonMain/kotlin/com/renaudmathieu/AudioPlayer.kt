package com.renaudmathieu

interface AudioPlayer {

    // Simple, high-level API
    fun load(track: Track) = setDataSource(track)
    fun play()
    fun pause()
    fun stop() = reset()
    fun seekTo(positionMs: Long)
    fun position(): Long = getCurrentPosition()
    fun duration(): Long = getDuration()
    val isPlaying: Boolean get() = position() > 0L && duration() >= 0L

    // Optional information
    val currentVolume: Float get() = 1f
    val isMuted: Boolean get() = false

    // Legacy API (still supported). Implementations can override either set.
    fun resume() = play()
    fun getCurrentPosition(): Long = 0L
    fun getDuration(): Long = 0L
    fun setDataSource(track: Track) {}
    fun reset() {}
    fun release()
}
