package com.info85.live77.ui.channels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.info85.live77.R
import com.info85.live77.databinding.ItemChannelBinding
import com.info85.live77.model.Channel

class ChannelAdapter(
    private val onChannelClick: (Channel) -> Unit
) : ListAdapter<Channel, ChannelAdapter.ChannelViewHolder>(ChannelDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val binding = ItemChannelBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChannelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChannelViewHolder(
        private val binding: ItemChannelBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(channel: Channel) {
            binding.tvChannelName.text = channel.name
            binding.tvChannelGroup.text = channel.group.orEmpty()

            if (!channel.logoUrl.isNullOrBlank()) {
                Glide.with(binding.ivChannelLogo)
                    .load(channel.logoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_channel_placeholder)
                    .error(R.drawable.ic_channel_placeholder)
                    .into(binding.ivChannelLogo)
            } else {
                Glide.with(binding.ivChannelLogo).clear(binding.ivChannelLogo)
                binding.ivChannelLogo.setImageResource(R.drawable.ic_channel_placeholder)
            }

            binding.root.setOnClickListener { onChannelClick(channel) }
        }
    }

    private class ChannelDiffCallback : DiffUtil.ItemCallback<Channel>() {
        override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean =
            oldItem.url == newItem.url

        override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean =
            oldItem == newItem
    }
}
