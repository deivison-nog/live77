package com.info85.live77.ui.player

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.info85.live77.R
import com.info85.live77.databinding.ActivityPlayerBinding
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STREAM_URL = "extra_stream_url"
        const val EXTRA_CHANNEL_NAME = "extra_channel_name"
        private const val CONTROLS_HIDE_DELAY_MS = 3_000L
        private const val RECONNECT_DELAY_MS = 3_000L
    }

    private lateinit var binding: ActivityPlayerBinding
    private var libVLC: LibVLC? = null
    private var mediaPlayer: MediaPlayer? = null
    private var streamUrl: String? = null

    private val hideHandler = Handler(Looper.getMainLooper())
    private val hideOverlayRunnable = Runnable { hideOverlay() }
    private val reconnectRunnable = Runnable { streamUrl?.let { reinitPlayer(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enterFullscreen()

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        streamUrl = intent.getStringExtra(EXTRA_STREAM_URL)
        if (streamUrl.isNullOrBlank()) {
            finish()
            return
        }

        val channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME) ?: getString(R.string.app_name)
        binding.tvChannelName.text = channelName

        binding.vlcVideoLayout.setOnClickListener { toggleOverlay() }
        binding.overlayControls.setOnClickListener { toggleOverlay() }
        binding.btnPlayPause.setOnClickListener { togglePlayPause() }

        initPlayer(streamUrl!!)
    }

    private fun enterFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun initPlayer(url: String) {
        binding.tvError.visibility = View.GONE
        binding.progressBuffering.visibility = View.VISIBLE

        val options = arrayListOf(
            "--network-caching=3000",
            "--live-caching=3000",
            "--clock-jitter=0",
            "--clock-synchro=0",
            "--no-drop-late-frames",
            "--no-skip-frames",
            "--rtsp-tcp"
        )
        libVLC = LibVLC(this, options)
        mediaPlayer = MediaPlayer(libVLC!!).also { player ->
            player.attachViews(binding.vlcVideoLayout, null, false, false)

            val media = Media(libVLC!!, Uri.parse(url)).apply {
                setHWDecoderEnabled(true, false)
                addOption(":network-caching=3000")
            }
            player.media = media
            media.release()

            player.setEventListener { event ->
                runOnUiThread {
                    when (event.type) {
                        MediaPlayer.Event.Buffering -> {
                            val pct = event.buffering
                            if (pct < 100f) {
                                binding.progressBuffering.visibility = View.VISIBLE
                            } else {
                                binding.progressBuffering.visibility = View.GONE
                            }
                        }
                        MediaPlayer.Event.Playing -> {
                            binding.progressBuffering.visibility = View.GONE
                            binding.tvError.visibility = View.GONE
                            updatePlayPauseIcon(isPlaying = true)
                            showOverlayBriefly()
                        }
                        MediaPlayer.Event.Paused,
                        MediaPlayer.Event.Stopped -> {
                            updatePlayPauseIcon(isPlaying = false)
                        }
                        MediaPlayer.Event.EncounteredError -> {
                            binding.progressBuffering.visibility = View.GONE
                            binding.tvError.text = getString(R.string.error_playback, "")
                            binding.tvError.visibility = View.VISIBLE
                            scheduleReconnect()
                        }
                    }
                }
            }

            player.play()
        }
    }

    /**
     * Fully releases the current player resources and reinitialises with the same URL.
     * Live HLS streams cannot be paused and resumed — the server playlist advances while
     * paused, so a clean restart is the only reliable way to unfreeze playback.
     */
    private fun reinitPlayer(url: String) {
        releasePlayer()
        initPlayer(url)
    }

    private fun releasePlayer() {
        hideHandler.removeCallbacks(reconnectRunnable)
        mediaPlayer?.run {
            stop()
            detachViews()
            release()
        }
        libVLC?.release()
        mediaPlayer = null
        libVLC = null
    }

    private fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            // For live streams a stopped/ended player needs a full reinit, not just play()
            streamUrl?.let { reinitPlayer(it) }
        }
        scheduleHideOverlay()
    }

    private fun scheduleReconnect() {
        hideHandler.removeCallbacks(reconnectRunnable)
        hideHandler.postDelayed(reconnectRunnable, RECONNECT_DELAY_MS)
    }

    private fun updatePlayPauseIcon(isPlaying: Boolean) {
        binding.btnPlayPause.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private fun showOverlayBriefly() {
        binding.overlayControls.visibility = View.VISIBLE
        scheduleHideOverlay()
    }

    private fun toggleOverlay() {
        if (binding.overlayControls.visibility == View.VISIBLE) {
            hideOverlay()
        } else {
            showOverlayBriefly()
        }
    }

    private fun hideOverlay() {
        binding.overlayControls.visibility = View.GONE
        hideHandler.removeCallbacks(hideOverlayRunnable)
    }

    private fun scheduleHideOverlay() {
        hideHandler.removeCallbacks(hideOverlayRunnable)
        hideHandler.postDelayed(hideOverlayRunnable, CONTROLS_HIDE_DELAY_MS)
    }

    override fun onResume() {
        super.onResume()
        enterFullscreen()
        // Always do a full reinit on resume: live HLS playlists advance while the
        // app is in the background, so pause→play produces 0 decoded frames.
        streamUrl?.let { reinitPlayer(it) }
    }

    override fun onPause() {
        super.onPause()
        hideHandler.removeCallbacks(hideOverlayRunnable)
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideHandler.removeCallbacks(hideOverlayRunnable)
        hideHandler.removeCallbacks(reconnectRunnable)
        releasePlayer()
    }
}
