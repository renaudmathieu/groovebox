package com.renaudmathieu

interface AudioPlayer {

    fun load(track: Track)
    fun play()
    fun pause()
    fun stop()
    fun seekTo(positionMs: Long)
    fun position(): Long
    fun duration(): Long
    val currentVolume: Float get() = 1f
    val isMuted: Boolean get() = false
    fun release()
    
}
