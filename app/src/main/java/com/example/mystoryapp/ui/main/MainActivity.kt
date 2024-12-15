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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mystoryapp.AuthViewModelFactory
import com.example.mystoryapp.R
import com.example.mystoryapp.databinding.ActivityMainBinding
import com.example.mystoryapp.di.Injection
import com.example.mystoryapp.ui.AuthViewModel
import com.example.mystoryapp.ui.login.LoginActivity
import com.example.mystoryapp.ui.add.AddStoryActivity
import com.example.mystoryapp.ui.location.MapsActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var authViewModel: AuthViewModel
    private lateinit var storyViewModel: StoryViewModel
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = Injection.provideRepository(this)
        authViewModel = ViewModelProvider(this, AuthViewModelFactory(repository))[AuthViewModel::class.java]
        storyViewModel = ViewModelProvider(this, StoryViewModelFactory(repository))[StoryViewModel::class.java]

        setupRecyclerView()

        storyViewModel.getToken()
        storyViewModel.token.observe(this) { token ->
            if (token.isNullOrEmpty()) {
                navigateToLogin()
            } else {
                observeNetworkStatus()
                observeViewModel()
            }
        }

        binding.btnAddStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }

        binding.rvStories.adapter = storyAdapter.withLoadStateFooter(
            footer = LoadingStateAdapter { storyAdapter.retry() }
        )
    }

    private fun observeViewModel() {
        storyViewModel.stories.observe(this) { pagingData ->
            storyAdapter.submitData(lifecycle, pagingData)
        }

        lifecycleScope.launch {
            storyAdapter.loadStateFlow.collectLatest { loadStates ->
                binding.loadingContainer.isVisible = loadStates.refresh is LoadState.Loading
                binding.errorText.isVisible = loadStates.refresh is LoadState.Error
            }
        }

        storyViewModel.isConnected.observe(this) { isConnected ->
            binding.errorText.isVisible = !isConnected
            binding.errorText.text = if (isConnected) "" else getString(R.string.no_internet_connection)
        }
    }


    private fun observeNetworkStatus() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder().build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                storyViewModel.updateConnectionStatus(true)
                storyAdapter.retry()
            }

            override fun onLost(network: Network) {
                storyViewModel.updateConnectionStatus(false)
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
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
}



