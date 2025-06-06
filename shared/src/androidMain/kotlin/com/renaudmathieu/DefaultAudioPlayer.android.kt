
package com.renaudmathieu

import android.media.AudioManager
import android.media.MediaPlayer
import java.io.IOException

class DefaultAudioPlayer() : AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var _currentVolume: Float = 1.0f
    private var _isMuted: Boolean = false
    private var isPrepared: Boolean = false
    private var pendingPlay: Boolean = false

    override val currentVolume: Float get() = _currentVolume
    override val isMuted: Boolean get() = _isMuted

    override fun play() {
        if (isPrepared) mediaPlayer?.start() else pendingPlay = true
    }

    override fun pause() {
        mediaPlayer?.pause()
    }

    override fun stop() {
        mediaPlayer?.stop()
        isPrepared = false
        pendingPlay = false
    }

    override fun resume() {
        if (mediaPlayer?.isPlaying == false && isPrepared) {
            mediaPlayer?.start()
        }
    }

    override fun seekTo(positionMs: Long) {
        if (isPrepared) {
            mediaPlayer?.seekTo(positionMs.toInt())
        }
    }

    override fun getCurrentPosition(): Long {
        return if (isPrepared) {
            mediaPlayer?.currentPosition?.toLong() ?: 0L
        } else {
            0L
        }
    }

    override fun getDuration(): Long {
        return if (isPrepared) {
            mediaPlayer?.duration?.toLong() ?: 0L
        } else {
            0L
        }
    }

    override fun setDataSource(track: Track) {
        try {
            // Reset previous state
            reset()
            isPrepared = false
            pendingPlay = false

            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)

                // Set up listeners
                setOnPreparedListener { mp ->
                    isPrepared = true
                    // Apply current volume settings
                    if (_isMuted) {
                        mp.setVolume(0f, 0f)
                    } else {
                        mp.setVolume(_currentVolume, _currentVolume)
                    }

                    // Auto-play if play was called before preparation
                    if (pendingPlay) {
                        mp.start()
                        pendingPlay = false
                    }
                }

                setOnErrorListener { mp, what, extra ->
                    Logger.e("MediaPlayer error: what=$what, extra=$extra", tag = "DefaultAudioPlayer")
                    isPrepared = false
                    pendingPlay = false
                    true // Return true to indicate error was handled
                }

                setOnCompletionListener { mp ->
                    // Handle playback completion if needed
                    Logger.i("Playback completed", tag = "DefaultAudioPlayer")
                }

                // Set data source and prepare asynchronously
                setDataSource(track.url)
                prepareAsync()
            }
        } catch (e: IOException) {
            Logger.e("Error setting data source: ${e.message}", e, tag = "DefaultAudioPlayer")
            isPrepared = false
            pendingPlay = false
        } catch (e: IllegalStateException) {
            Logger.e("IllegalStateException setting data source: ${e.message}", e, tag = "DefaultAudioPlayer")
            isPrepared = false
            pendingPlay = false
        }
    }

    override fun reset() {
        try {
            mediaPlayer?.reset()
            isPrepared = false
            pendingPlay = false
        } catch (e: IllegalStateException) {
            Logger.e("Error resetting MediaPlayer: ${e.message}", e, tag = "DefaultAudioPlayer")
        }
    }

    override fun release() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            isPrepared = false
            pendingPlay = false
        } catch (e: Exception) {
            Logger.e("Error releasing MediaPlayer: ${e.message}", e, tag = "DefaultAudioPlayer")
        }
    }

}
