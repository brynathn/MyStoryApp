package com.example.mystoryapp.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mystoryapp.data.Repository
import com.example.mystoryapp.response.StoryItem
import com.example.mystoryapp.Result
import kotlinx.coroutines.launch

class DetailStoryViewModel(private val repository: Repository) : ViewModel() {

    private val _storyDetail = MutableLiveData<Result<StoryItem>>()
    val storyDetail: LiveData<Result<StoryItem>> = _storyDetail

    fun fetchStoryDetail(storyId: String) {
        viewModelScope.launch {
            _storyDetail.value = Result.Loading
            val token = repository.getUserToken()
            if (!token.isNullOrEmpty()) {
                val result = repository.getStoryDetail(token, storyId)
                _storyDetail.value = result
            } else {
                _storyDetail.value = Result.Error("Token tidak ditemukan. Silakan login kembali.")
            }
        }
    }
}
