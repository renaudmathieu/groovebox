package com.renaudmathieu

interface AudioPlayer {

    val currentVolume: Float
    val isMuted: Boolean

    fun play()
    fun pause()
    fun stop()
    fun resume()
    fun seekTo(positionMs: Long)
    fun getCurrentPosition(): Long
    fun getDuration(): Long
    fun setDataSource(track: Track)
    fun reset()
    fun release()
    
}
