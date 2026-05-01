package com.live77.ui.player

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.live77.R
import com.live77.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STREAM_URL = "extra_stream_url"
        const val EXTRA_CHANNEL_NAME = "extra_channel_name"
    }

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val streamUrl = intent.getStringExtra(EXTRA_STREAM_URL)
        if (streamUrl.isNullOrBlank()) {
            finish()
            return
        }

        val channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME) ?: getString(R.string.app_name)
        supportActionBar?.apply {
            title = channelName
            setDisplayHomeAsUpEnabled(true)
        }

        initPlayer(streamUrl)
    }

    private fun initPlayer(url: String) {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            exoPlayer.setMediaItem(MediaItem.fromUri(url))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    binding.tvError.text =
                        getString(R.string.error_playback, error.message ?: "")
                    binding.tvError.visibility = View.VISIBLE
                }
            })
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private var wasPlayingBeforePause = true

    override fun onPause() {
        super.onPause()
        wasPlayingBeforePause = player?.isPlaying == true
        player?.pause()
    }

    override fun onResume() {
        super.onResume()
        // Only resume playback if the player was playing before the activity was paused;
        // this respects an intentional user pause.
        if (wasPlayingBeforePause) {
            player?.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
