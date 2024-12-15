package com.example.mystoryapp.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mystoryapp.data.Repository
import com.example.mystoryapp.response.StoryItem
import com.example.mystoryapp.AppResult
import kotlinx.coroutines.launch

class DetailStoryViewModel(private val repository: Repository) : ViewModel() {

    private val _storyDetail = MutableLiveData<AppResult<StoryItem>>()
    val storyDetail: LiveData<AppResult<StoryItem>> = _storyDetail

    fun fetchStoryDetail(storyId: String) {
        viewModelScope.launch {
            _storyDetail.value = AppResult.Loading
            val token = repository.getUserToken()
            if (!token.isNullOrEmpty()) {
                val result = repository.getStoryDetail(token, storyId)
                _storyDetail.value = result
            } else {
                _storyDetail.value = AppResult.Error("Token tidak ditemukan. Silakan login kembali.")
            }
        }
    }
}
