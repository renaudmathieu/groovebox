package com.renaudmathieu

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.*
import platform.CoreMedia.CMTime
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.beginReceivingRemoteControlEvents
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
class DefaultAudioPlayer : AudioPlayer {

    private val player: AVPlayer = AVPlayer()
    private val audioSession: AVAudioSession = AVAudioSession.sharedInstance()

    private val _position = MutableStateFlow(0L)
    override val position: StateFlow<Long> = _position.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    override val duration: StateFlow<Long> = _duration.asStateFlow()

    private var timeObserver: Any? = null

    init {
        try {
            audioSession.setCategory(AVAudioSessionCategoryPlayback, null)
            audioSession.setActive(true, null)
            UIApplication.sharedApplication.beginReceivingRemoteControlEvents()
        } catch (e: Exception) {
            Logger.e("Failed to setup audio session: ${e.message}")
        }
    }

    override val currentVolume: Float
        get() = player.volume()

    override suspend fun isMuted(): Boolean = player.isMuted()

    override fun play() {
        ensureTimeObserver()
        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun stop() {
        player.pause()
        // reset position to 0 and seek
        _position.value = 0L
        // fire and forget seek to start
        val time = CMTimeMakeWithSeconds(0.0, 1000)
        player.seekToTime(time)
    }

    override suspend fun seekTo(positionMs: Long) {
        val seconds = positionMs.toDouble() / 1000.0
        val time = CMTimeMakeWithSeconds(seconds, 1000)
        return suspendCancellableCoroutine { cont ->
            player.seekToTime(time) { _ ->
                if (!cont.isCompleted) cont.resume(Unit)
            }
        }
    }

    override suspend fun load(track: Track) {
        _position.value = 0L
        _duration.value = 0L
        try {
            val url = NSURL(string = track.url)
            val playerItem = AVPlayerItem(uRL = url)
            player.replaceCurrentItemWithPlayerItem(playerItem)
        } catch (e: Exception) {
            Logger.e("Failed to set data source: ${e.message}")
        }
        // Wait until duration is known or item is ready, with a timeout
        var attempts = 0
        while (attempts < 50) { // ~5 seconds
            val item = player.currentItem
            if (item != null) {
                val s = CMTimeGetSeconds(item.duration)
                if (!s.isNaN() && !s.isInfinite() && s > 0) {
                    _duration.value = (s * 1000).toLong()
                    break
                }
                // If status is ready, duration may still be 0 for streaming; keep waiting until > 0 or timeout
            }
            delay(100)
            attempts++
        }
        ensureTimeObserver()
    }

    override fun release() {
        if (timeObserver != null) {
            player.removeTimeObserver(timeObserver!!)
            timeObserver = null
        }
        // Clear current item and deactivate audio session
        player.replaceCurrentItemWithPlayerItem(null)
        try {
            audioSession.setActive(false, null)
        } catch (e: Exception) {
            Logger.e("Failed to deactivate audio session: ${e.message}")
        }
    }

    private fun ensureTimeObserver() {
        if (timeObserver != null) return
        val interval = CMTimeMakeWithSeconds(0.1, 1000)
        timeObserver = player.addPeriodicTimeObserverForInterval(interval, null) { time ->
            val seconds = CMTimeGetSeconds(time)
            _position.value = (seconds * 1000).toLong()
            val item = player.currentItem
            if (item != null) {
                val dSec = CMTimeGetSeconds(item.duration)
                if (!dSec.isNaN() && !dSec.isInfinite()) {
                    val ms = (dSec * 1000).toLong()
                    if (ms != duration.value) {
                        _duration.value = ms
                    }
                }
            }
        }
    }
}