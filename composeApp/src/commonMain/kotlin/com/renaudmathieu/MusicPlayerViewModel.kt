package com.renaudmathieu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MusicPlayerViewModel(
    private val repository: MusicPlayerRepository,
    private val audioPlayer: AudioPlayer,
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
        viewModelScope.launch {
            audioPlayer.seekTo(positionMs)
            _musicPlayerUiState.update { it.copy(currentPosition = positionMs) }
        }
    }

    fun updatePosition() {
        // Read hot state values from the player
        val currentPosition = audioPlayer.position.value
        val duration = audioPlayer.duration.value
        _musicPlayerUiState.update {
            it.copy(
                currentPosition = currentPosition,
                duration = duration
            )
        }
    }

    fun load(track: Track) {
        viewModelScope.launch {
            audioPlayer.load(track)
            _musicPlayerUiState.update { it.copy(track = track, currentPosition = 0L, duration = 0L, isPlaying = false) }
            // After loading, duration may not be immediately available (player prepares asynchronously).
            // Poll briefly until duration is known or timeout.
            repeat(30) { // ~3 seconds total
                val dur = audioPlayer.duration.value
                if (dur > 0) {
                    _musicPlayerUiState.update { it.copy(duration = dur) }
                    return@launch
                }
                delay(100)
            }
        }
    }

    fun release() {
        audioPlayer.release()
    }
}
