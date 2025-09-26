package com.renaudmathieu


import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemFailedToPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemStatusReadyToPlay
import platform.AVFoundation.AVPlayerItemStatusUnknown
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.addPeriodicTimeObserverForInterval
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.isMuted
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.removeTimeObserver
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVFoundation.timeControlStatus
import platform.AVFoundation.volume
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.beginReceivingRemoteControlEvents
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
class DefaultAudioPlayer : AudioPlayer {

    private val avPlayer: AVPlayer = AVPlayer()
    private var timeObserver: Any? = null
    private var positionUpdateJob: Job? = null
    private val _position = MutableStateFlow(0L)
    private var trackDuration: Long = 0L
    private lateinit var audioSession: AVAudioSession

    override val position: StateFlow<Long> = _position.asStateFlow()

    override val duration: Long
        get() = trackDuration

    override val currentVolume: Float
        get() = avPlayer.volume

    init {
        setupAudioSession()
    }

    private fun setupAudioSession() {
        try {
            UIApplication.sharedApplication.beginReceivingRemoteControlEvents()

            audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(
                category = AVAudioSessionCategoryPlayback,
                error = null,
            )
            audioSession.setActive(
                active = true,
                error = null,
            )
        } catch (e: Exception) {
            // Audio session setup failed, continue without it
        }
    }

    override suspend fun load(track: Track) = suspendCancellableCoroutine<Unit> { continuation ->
        try {
            // Clean up previous observers and reset state
            cleanup()
            trackDuration = 0L
            _position.value = 0L

            // Validate file if it's a local path
            if (!track.url.startsWith("http")) {
                val fileManager = NSFileManager.defaultManager
                if (!fileManager.fileExistsAtPath(track.url)) {
                    continuation.resume(Unit)
                    return@suspendCancellableCoroutine
                }

                // Check file size
                val attributes = fileManager.attributesOfItemAtPath(track.url, null)
                val fileSize = attributes?.get(NSFileSize) as? NSNumber
                if (fileSize?.longValue == 0L) {
                    continuation.resume(Unit)
                    return@suspendCancellableCoroutine
                }
            }

            val url = if (track.url.startsWith("http")) {
                NSURL.URLWithString(track.url)
            } else {
                NSURL.fileURLWithPath(track.url)
            } ?: run {
                continuation.resume(Unit)
                return@suspendCancellableCoroutine
            }

            val asset = AVURLAsset(url, mapOf("AVURLAssetOutOfBandMIMETypeKey" to "audio/mpeg"))
            val playerItem = AVPlayerItem(asset)

            // Add observer for player item failure
            val notificationCenter = NSNotificationCenter.defaultCenter
            val failureObserver = notificationCenter.addObserverForName(
                AVPlayerItemFailedToPlayToEndTimeNotification,
                `object` = playerItem,
                queue = null
            ) { _ ->
                // Player item failed, but we still resume to avoid hanging
                continuation.resume(Unit)
            }

            // Use a coroutine to poll the player item status instead of KVO
            val checkStatusJob = CoroutineScope(Dispatchers.Main).launch {
                var attempts = 0
                while (attempts < 50 && playerItem.status == AVPlayerItemStatusUnknown) {
                    delay(100)
                    attempts++
                }

                when (playerItem.status) {
                    AVPlayerItemStatusReadyToPlay -> {
                        val duration = playerItem.duration
                        val durationSeconds = CMTimeGetSeconds(duration)
                        if (!durationSeconds.isNaN() && !durationSeconds.isInfinite()) {
                            trackDuration = (durationSeconds * 1000).toLong()
                        }
                        setupTimeObserver()
                    }

                    else -> {
                        // Failed or still unknown after timeout
                    }
                }

                notificationCenter.removeObserver(failureObserver)
                continuation.resume(Unit)
            }

            avPlayer.replaceCurrentItemWithPlayerItem(playerItem)

            continuation.invokeOnCancellation {
                notificationCenter.removeObserver(failureObserver)
                checkStatusJob.cancel()
            }

        } catch (e: Exception) {
            continuation.resume(Unit)
        }
    }

    override suspend fun seekTo(positionMs: Long) {
        val time = CMTimeMakeWithSeconds(positionMs.toDouble() / 1000.0, 1000)
        avPlayer.seekToTime(time)
        _position.value = positionMs
    }

    override suspend fun isMuted(): Boolean {
        return avPlayer.isMuted()
    }

    override fun play() {
        avPlayer.play()
        startPositionUpdates()
    }

    override fun pause() {
        avPlayer.pause()
        stopPositionUpdates()
    }

    override fun stop() {
        avPlayer.pause()
        val zeroTime = CMTimeMakeWithSeconds(0.0, 1000)
        avPlayer.seekToTime(zeroTime)
        _position.value = 0L
        stopPositionUpdates()
    }

    override fun release() {
        cleanup()
        try {
            audioSession.setActive(false, null)
        } catch (e: Exception) {
            // Ignore audio session deactivation errors
        }
    }

    private fun cleanup() {
        stopPositionUpdates()

        timeObserver?.let { observer ->
            avPlayer.removeTimeObserver(observer)
            timeObserver = null
        }

        trackDuration = 0L
        _position.value = 0L
    }

    private fun setupTimeObserver() {
        val interval = CMTimeMakeWithSeconds(0.1, 1000) // 100ms intervals

        timeObserver = avPlayer.addPeriodicTimeObserverForInterval(
            interval = interval,
            queue = null
        ) { time ->
            val timeSeconds = CMTimeGetSeconds(time)
            if (!timeSeconds.isNaN() && !timeSeconds.isInfinite()) {
                val positionMs = (timeSeconds * 1000).toLong()
                _position.value = positionMs
            }
        }
    }

    private fun startPositionUpdates() {
        // AVPlayer's time observer handles most position updates
        // This is a backup mechanism for consistency
        stopPositionUpdates()
        positionUpdateJob = CoroutineScope(Dispatchers.Main).launch {
            while (avPlayer.timeControlStatus == AVPlayerTimeControlStatusPlaying) {
                val currentTime = avPlayer.currentTime()
                val timeSeconds = CMTimeGetSeconds(currentTime)
                if (!timeSeconds.isNaN() && !timeSeconds.isInfinite()) {
                    val positionMs = (timeSeconds * 1000).toLong()
                    _position.value = positionMs
                }
                delay(100)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
}