package com.example.mystoryapp.ui.main

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mystoryapp.R
import com.example.mystoryapp.databinding.ItemStoryBinding
import com.example.mystoryapp.response.StoryItem
import com.example.mystoryapp.ui.detail.DetailStoryActivity
import androidx.core.util.Pair

class StoryAdapter : ListAdapter<StoryItem, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        holder.bind(story)
    }

    class StoryViewHolder(private val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        private var imgPhoto: ImageView = itemView.findViewById(R.id.iv_item_photo)
        private var tvName: TextView = itemView.findViewById(R.id.tv_item_name)
        private var tvDescription: TextView = itemView.findViewById(R.id.tv_item_desc)

        fun bind(story: StoryItem) {
            binding.tvItemName.text = story.name
            binding.tvItemDesc.text = story.description
            Glide.with(binding.root)
                .load(story.photoUrl)
                .into(binding.ivItemPhoto)

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailStoryActivity::class.java).apply {
                    putExtra(DetailStoryActivity.EXTRA_STORY_ID, story.id)
                }
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(imgPhoto, "story"),
                        Pair(tvName, "name"),
                        Pair(tvDescription, "description"),
                    )
                itemView.context.startActivity(intent,  optionsCompat.toBundle())
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryItem>() {
            override fun areItemsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean = oldItem == newItem
        }
    }
}

