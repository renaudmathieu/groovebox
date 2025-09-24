package com.renaudmathieu

data class MusicPlayerUiState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val track: Track? = null,
)
