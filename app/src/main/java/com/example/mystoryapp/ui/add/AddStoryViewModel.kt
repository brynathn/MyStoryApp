package com.example.mystoryapp.ui.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mystoryapp.data.Repository
import com.example.mystoryapp.Result
import kotlinx.coroutines.launch
import java.io.File

class AddStoryViewModel(private val repository: Repository) : ViewModel() {

    val uploadResult = MutableLiveData<Result<Unit>>()

    fun uploadStory(description: String, imageFile: File, lat: Float? = null, lon: Float? = null) {
        viewModelScope.launch {
            val token = repository.getUserToken() ?: return@launch
            uploadResult.value = Result.Loading
            val result = repository.addStory(description, imageFile, token, lat, lon)
            uploadResult.value = result
        }
    }
}

