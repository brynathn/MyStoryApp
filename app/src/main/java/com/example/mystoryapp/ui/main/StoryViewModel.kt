package com.example.mystoryapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mystoryapp.data.Repository
import com.example.mystoryapp.response.StoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StoryViewModel(private val repository: Repository) : ViewModel() {

    private val _token = MutableLiveData<String?>()
    val token: LiveData<String?> get() = _token

    val isDatabaseEmpty = MutableLiveData<Boolean>()

    init {
        viewModelScope.launch {
            val storyCount = withContext(Dispatchers.IO) {
                repository.getStoryCount()
            }
            isDatabaseEmpty.postValue(storyCount == 0)
        }
    }

    fun getToken() {
        viewModelScope.launch {
            val userToken = repository.getUserToken()
            _token.postValue(userToken)
        }
    }

    val stories: LiveData<PagingData<StoryItem>> = liveData {
        val token = repository.getUserToken()
        if (!token.isNullOrEmpty()) {
            emitSource(
                repository.getStoriesPagingData(token)
                    .cachedIn(viewModelScope)
            )
        } else {
            emit(PagingData.empty())
        }
    }


    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> get() = _isConnected

    fun updateConnectionStatus(isConnected: Boolean) {
        _isConnected.postValue(isConnected)
    }
}




