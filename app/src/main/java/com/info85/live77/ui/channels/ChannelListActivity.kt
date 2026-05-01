package com.info85.live77.ui.channels

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.info85.live77.R
import com.info85.live77.databinding.ActivityChannelListBinding
import com.info85.live77.ui.player.PlayerActivity

class ChannelListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChannelListBinding
    private val viewModel: ChannelListViewModel by viewModels()
    private lateinit var adapter: ChannelAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChannelListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.label_channels)

        adapter = ChannelAdapter { channel ->
            startActivity(
                Intent(this, PlayerActivity::class.java).apply {
                    putExtra(PlayerActivity.EXTRA_STREAM_URL, channel.url)
                    putExtra(PlayerActivity.EXTRA_CHANNEL_NAME, channel.name)
                }
            )
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.btnRetry.setOnClickListener {
            viewModel.loadChannels()
        }

        viewModel.channels.observe(this) { channels ->
            adapter.submitList(channels)
            binding.recyclerView.visibility = if (channels.isNotEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.loading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            if (loading) {
                binding.tvError.visibility = View.GONE
                binding.btnRetry.visibility = View.GONE
                binding.tvEmpty.visibility = View.GONE
            } else {
                // Show empty state only when done loading and no channels/error
                val hasChannels = (viewModel.channels.value?.isNotEmpty()) == true
                val hasError = viewModel.error.value != null
                binding.tvEmpty.visibility =
                    if (!hasChannels && !hasError) View.VISIBLE else View.GONE
            }
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                binding.tvError.text = error
                binding.tvError.visibility = View.VISIBLE
                binding.btnRetry.visibility = View.VISIBLE
            } else {
                binding.tvError.visibility = View.GONE
                binding.btnRetry.visibility = View.GONE
            }
        }

        viewModel.loadChannels()
    }
}
