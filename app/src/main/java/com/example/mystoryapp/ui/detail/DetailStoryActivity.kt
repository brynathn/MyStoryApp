package com.example.mystoryapp.ui.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.mystoryapp.databinding.ActivityDetailStoryBinding
import com.example.mystoryapp.di.Injection
import com.example.mystoryapp.response.StoryItem
import com.example.mystoryapp.Result

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding
    private lateinit var viewModel: DetailStoryViewModel

    companion object {
        const val EXTRA_STORY_ID = "extra_story_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = Injection.provideRepository(this)
        viewModel = ViewModelProvider(this, DetailStoryViewModelFactory(repository))[DetailStoryViewModel::class.java]

        val storyId = intent.getStringExtra(EXTRA_STORY_ID)
        if (storyId != null) {
            observeStoryDetail()
            viewModel.fetchStoryDetail(storyId)
        } else {
            Toast.makeText(this, "Detail cerita tidak ditemukan.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun observeStoryDetail() {
        viewModel.storyDetail.observe(this) { result ->
            when (result) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> {
                    showLoading(false)
                    displayStory(result.data)
                }
                is Result.Error -> {
                    showLoading(false)
                    Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayStory(story: StoryItem) {
        binding.tvDetailName.text = story.name
        binding.tvDetailDescription.text = story.description
        Glide.with(this)
            .load(story.photoUrl)
            .into(binding.ivDetailPhoto)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}

