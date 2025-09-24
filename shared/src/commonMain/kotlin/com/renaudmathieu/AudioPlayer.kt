package com.renaudmathieu

import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {

    suspend fun load(track: Track)

    fun play()
    fun pause()
    fun stop()
    fun release()

    suspend fun seekTo(positionMs: Long)

    val position: StateFlow<Long> // ms
    val duration: Long // 0 until known
    val currentVolume: Float
    suspend fun isMuted(): Boolean
}
