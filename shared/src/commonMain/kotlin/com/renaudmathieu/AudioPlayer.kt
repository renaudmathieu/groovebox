package com.renaudmathieu

import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {

    // Suspends until prepared/ready
    suspend fun load(track: Track)

    // Immediate controls (usually non-suspending)
    fun play()
    fun pause()
    fun stop()
    fun release()

    // Suspends until seek completes
    suspend fun seekTo(positionMs: Long)

    // Hot streams (replay last known value)
    val position: StateFlow<Long> // ms
    val duration: StateFlow<Long> // 0 until known

    val currentVolume: Float
    suspend fun isMuted(): Boolean
}
