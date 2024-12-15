package com.example.mystoryapp.ui.main

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mystoryapp.AuthViewModelFactory
import com.example.mystoryapp.R
import com.example.mystoryapp.databinding.ActivityMainBinding
import com.example.mystoryapp.di.Injection
import com.example.mystoryapp.ui.AuthViewModel
import com.example.mystoryapp.ui.login.LoginActivity
import com.example.mystoryapp.AppResult
import com.example.mystoryapp.ui.add.AddStoryActivity
import com.example.mystoryapp.ui.location.MapsActivity

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
        observeNetworkStatus()
        observeViewModel()

        binding.btnAddStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        storyViewModel.stories.observe(this) { result ->
            when (result) {
                is AppResult.Loading -> {
                    showLoading(true)
                    showError(false)
                }
                is AppResult.Success -> {
                    showLoading(false)
                    showError(false)
                    storyAdapter.submitList(result.data)
                }
                is AppResult.Error -> {
                    showLoading(false)
                    showError(true)
                }
            }
        }
    }

    private fun observeNetworkStatus() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder().build()

        val isConnected = connectivityManager.activeNetwork != null
        storyViewModel.updateConnectionStatus(isConnected)

        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                storyViewModel.updateConnectionStatus(true)
            }

            override fun onLost(network: Network) {
                storyViewModel.updateConnectionStatus(false)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_maps -> {
                navigateToMaps()
                true
            }
            R.id.language -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }
            R.id.logout -> {
                authViewModel.logout()
                Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
                navigateToLogin()
                true
            }
            else -> super.onOptionsItemSelected(item)
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

    private fun navigateToMaps() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(isVisible: Boolean) {
        binding.errorText.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}


