package com.renaudmathieu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class MusicPlayerViewModel(
    private val repository: MusicPlayerRepository,
) : ViewModel() {

    private val _musicPlayerUiState = MutableStateFlow(MusicPlayerUiState())
    val musicPlayerUiState: StateFlow<MusicPlayerUiState> = _musicPlayerUiState.asStateFlow()

    val initialTrack: StateFlow<Track?> = flow {
        try {
            emit(repository.loadFirstTrack())
        } catch (t: Throwable) {
            println("Failed to load initial track: ${t.message}")
            emit(null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )
    
    fun togglePlayPause() {
        if (_musicPlayerUiState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        repository.play()
        _musicPlayerUiState.update { it.copy(isPlaying = true) }
    }

    fun pause() {
        repository.pause()
        _musicPlayerUiState.update { it.copy(isPlaying = false) }
    }

    fun stop() {
        repository.stop()
        _musicPlayerUiState.update { it.copy(isPlaying = false) }
    }

    fun seekTo(positionMs: Long) {
        repository.seekTo(positionMs)
        _musicPlayerUiState.update { it.copy(currentPosition = positionMs) }
    }

    fun updatePosition() {
        val currentPosition = repository.getCurrentPosition()
        val duration = repository.getDuration()
        _musicPlayerUiState.update {
            it.copy(
                currentPosition = currentPosition,
                duration = duration
            )
        }
    }

    fun setDataSource(track: Track) {
        repository.setDataSource(track)
        _musicPlayerUiState.update { it.copy(track = track) }
    }

    fun release() {
        repository.release()
    }
}
