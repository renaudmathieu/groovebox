package com.renaudmathieu

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MusicPlayerViewModel(
    private val repository: MusicPlayerRepository
) : ViewModel(), KoinComponent {

    private val audioPlayer: AudioPlayer by inject()

    private val _musicPlayerUiState = MutableStateFlow(MusicPlayerUiState())
    val musicPlayerUiState: StateFlow<MusicPlayerUiState> = _musicPlayerUiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val track = repository.loadFirstTrack()
                if (track != null) {
                    setDataSource(track)
                }
            } catch (t: Throwable) {
                // Log or ignore for now
                println("Failed to load initial track: ${t.message}")
            }
        }
    }
    
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
