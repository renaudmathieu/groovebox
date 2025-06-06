package com.renaudmathieu

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.*
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.beginReceivingRemoteControlEvents

@OptIn(ExperimentalForeignApi::class)
class DefaultAudioPlayer : AudioPlayer {

    private val player: AVPlayer = AVPlayer()
    private val audioSession: AVAudioSession = AVAudioSession.sharedInstance()

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
    override val isMuted: Boolean
        get() = player.isMuted()

    override fun play() {
        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun stop() {
        player.pause()
        seekTo(0)
    }

    override fun resume() {
        player.play()
    }

    override fun seekTo(positionMs: Long) {
        val seconds = positionMs.toDouble() / 1000.0
        val time = CMTimeMakeWithSeconds(seconds, 1000)
        player.seekToTime(time)
    }

    override fun getCurrentPosition(): Long {
        if (player.currentItem == null) return 0L
        val time = player.currentTime()
        val seconds = CMTimeGetSeconds(time)
        return (seconds * 1000).toLong()
    }

    override fun getDuration(): Long {
        val duration = player.currentItem?.duration ?: return 0L
        val seconds = CMTimeGetSeconds(duration)
        return if (seconds.isNaN() || seconds.isInfinite()) 0L else (seconds * 1000).toLong()
    }

    override fun setDataSource(track: Track) {
        try {
            val url = NSURL(string = track.url)
            val playerItem = AVPlayerItem(uRL = url)
            player.replaceCurrentItemWithPlayerItem(playerItem)
        } catch (e: Exception) {
            Logger.e("Failed to set data source: ${e.message}")
        }
    }

    override fun reset() {
        player.replaceCurrentItemWithPlayerItem(null)
    }

    override fun release() {
        reset()
        try {
            audioSession.setActive(false, null)
        } catch (e: Exception) {
            Logger.e("Failed to deactivate audio session: ${e.message}")
        }
    }
}