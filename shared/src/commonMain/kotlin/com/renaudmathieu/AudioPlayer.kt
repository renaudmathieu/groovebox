package com.renaudmathieu

import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {

    val position: StateFlow<Long>
    val duration: Long
    val currentVolume: Float
    suspend fun load(track: Track)
    fun play()
    fun pause()
    fun stop()
    fun release()
    suspend fun seekTo(positionMs: Long)
    suspend fun isMuted(): Boolean
}
