package com.example.mystoryapp.ui.add

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mystoryapp.data.Repository
import com.example.mystoryapp.AppResult
import kotlinx.coroutines.launch
import java.io.File

class AddStoryViewModel(private val repository: Repository, private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val uploadAppResult = MutableLiveData<AppResult<Unit>>()

    var currentImageUri: Uri?
        get() = savedStateHandle.get<Uri>("CURRENT_IMAGE_URI")
        set(value) {
            savedStateHandle["CURRENT_IMAGE_URI"] = value
        }
    var selectedFile: File? = null

    fun uploadStory(description: String, imageFile: File, lat: Float? = null, lon: Float? = null) {
        viewModelScope.launch {
            val token = repository.getUserToken() ?: return@launch
            uploadAppResult.value = AppResult.Loading
            val result = repository.addStory(description, imageFile, token, lat, lon)
            uploadAppResult.value = result
        }
    }
}

