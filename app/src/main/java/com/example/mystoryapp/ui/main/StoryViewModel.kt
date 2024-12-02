package com.example.mystoryapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mystoryapp.data.Repository
import com.example.mystoryapp.response.StoryItem
import kotlinx.coroutines.launch
import com.example.mystoryapp.Result

class StoryViewModel(private val repository: Repository) : ViewModel() {

    private val _stories = MutableLiveData<Result<List<StoryItem>>>()
    val stories: LiveData<Result<List<StoryItem>>> = _stories

    private val _isConnected = MutableLiveData<Boolean>()

    private var hasFetchedData = false

    init {
        fetchStoriesIfNeeded()
    }

    private fun fetchStoriesIfNeeded() {
        if (!hasFetchedData) {
            fetchStories()
        }
    }

    private fun fetchStories() {
        viewModelScope.launch {
            val token = repository.getUserToken()
            if (token != null) {
                _stories.value = Result.Loading
                _stories.value = repository.getAllStories(token)
                hasFetchedData = true
            } else {
                _stories.value = Result.Error("Token tidak tersedia")
            }
        }
    }

    fun updateConnectionStatus(isConnected: Boolean) {
        val wasDisconnected = _isConnected.value == false
        _isConnected.postValue(isConnected)

        if (isConnected && (wasDisconnected || !hasFetchedData)) {
            fetchStories()
        }
    }
}



