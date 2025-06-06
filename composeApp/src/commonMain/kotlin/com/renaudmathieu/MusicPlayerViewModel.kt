package com.renaudmathieu

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class MusicPlayerViewModel(
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _musicPlayerUiState = MutableStateFlow(MusicPlayerUiState())
    val musicPlayerUiState: StateFlow<MusicPlayerUiState> = _musicPlayerUiState.asStateFlow()
    
    // Methods
    fun togglePlayPause() {
        if (_musicPlayerUiState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        audioPlayer.play()
        _musicPlayerUiState.update { it.copy(isPlaying = true) }
    }

    fun pause() {
        audioPlayer.pause()
        _musicPlayerUiState.update { it.copy(isPlaying = false) }
    }

    fun stop() {
        audioPlayer.stop()
        _musicPlayerUiState.update { it.copy(isPlaying = false) }
    }

    fun seekTo(positionMs: Long) {
        audioPlayer.seekTo(positionMs)
        _musicPlayerUiState.update { it.copy(currentPosition = positionMs) }
    }

    fun updatePosition() {
        val currentPosition = audioPlayer.getCurrentPosition()
        val duration = audioPlayer.getDuration()
        _musicPlayerUiState.update {
            it.copy(
                currentPosition = currentPosition,
                duration = duration
            )
        }
    }

    fun setDataSource(track: Track) {
        audioPlayer.setDataSource(track)
        _musicPlayerUiState.update { it.copy(track = track) }
    }

    fun release() {
        audioPlayer.release()
    }
}
