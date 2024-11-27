package com.example.mystoryapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mystoryapp.AuthViewModelFactory
import com.example.mystoryapp.databinding.ActivityMainBinding
import com.example.mystoryapp.di.Injection
import com.example.mystoryapp.ui.AuthViewModel
import com.example.mystoryapp.ui.login.LoginActivity
import com.example.mystoryapp.Result
import com.example.mystoryapp.ui.add.AddStoryActivity
import com.example.mystoryapp.ui.widget.MyStoryWidget

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var authViewModel: AuthViewModel
    private lateinit var storyViewModel: StoryViewModel
    private lateinit var storyAdapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = Injection.provideRepository(this)
        authViewModel = ViewModelProvider(this, AuthViewModelFactory(repository))[AuthViewModel::class.java]

        storyViewModel = ViewModelProvider(this, StoryViewModelFactory(repository))[StoryViewModel::class.java]

        setupRecyclerView()

        storyViewModel.stories.observe(this) { result ->
            when (result) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> {
                    showLoading(false)
                    storyAdapter.submitList(result.data)
                }
                is Result.Error -> {
                    showLoading(false)
                    Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }

        storyViewModel.fetchStories()

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show()
            MyStoryWidget().refreshWidget(this)
            navigateToLogin()
        }

        binding.btnAddStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        if (intent.getBooleanExtra("REFRESH_LIST", false)) {
            storyViewModel.fetchStories()
            intent.removeExtra("REFRESH_LIST")
        }
    }


    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter()
        binding.rvStories.apply {
            adapter = storyAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}

