package com.example.mystoryapp.ui.location

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mystoryapp.AppResult
import com.example.mystoryapp.data.Repository
import com.example.mystoryapp.response.StoryItem
import kotlinx.coroutines.launch


class MapsViewModel(private val repository: Repository) : ViewModel() {
    private val _stories = MutableLiveData<AppResult<List<StoryItem>>>()
    val stories: MutableLiveData<AppResult<List<StoryItem>>> get() = _stories

    fun fetchStoriesWithLocation() {
        viewModelScope.launch {
            val token = repository.getUserToken()
            if (token != null){
                _stories.value = AppResult.Loading
                _stories.value =  repository.getStoriesWithLocation(token)
            }else {
                _stories.value = AppResult.Error("Token tidak tersedia")
            }
        }
    }
}
