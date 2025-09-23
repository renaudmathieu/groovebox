package com.renaudmathieu

data class MusicPlayerUiState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val track: Track = Track(
        id = "1",
        title = "Make Love",
        artist = "Daft Punk",
//        url = "http://10.0.2.2:$SERVER_PORT/track",
        url = "http://127.0.0.1:$SERVER_PORT/track",
        duration = 300000 // 5 minutes in milliseconds
    )
)
