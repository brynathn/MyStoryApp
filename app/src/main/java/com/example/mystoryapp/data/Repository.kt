package com.example.mystoryapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.mystoryapp.UserPreferences
import com.example.mystoryapp.request.LoginRequest
import com.example.mystoryapp.request.SignUpRequest
import com.example.mystoryapp.retrofit.ApiService
import kotlinx.coroutines.flow.map
import com.example.mystoryapp.Result
import com.example.mystoryapp.response.StoryItem
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class Repository(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences
) {

    suspend fun getUserToken(): String? {
        return userPreferences.getUserToken().firstOrNull()
    }

    suspend fun getAllStories(token: String): Result<List<StoryItem>> {
        return try {
            val response = apiService.getAllStories("Bearer $token")
            if (!response.error) {
                Result.Success(response.listStory ?: emptyList())
            } else {
                Result.Error(response.message)
            }
        } catch (e: Exception) {
            Result.Error("Gagal memuat cerita: ${e.message}")
        }
    }

    suspend fun getStoryDetail(token: String, storyId: String): Result<StoryItem> {
        return try {
            val response = apiService.getStoryDetail("Bearer $token", storyId)
            if (!response.error && response.story != null) {
                Result.Success(response.story)
            } else {
                Result.Error(response.message)
            }
        } catch (e: Exception) {
            Result.Error("Gagal memuat detail cerita: ${e.message}")
        }
    }

    suspend fun addStory(
        description: String,
        imageFile: File,
        token: String,
        lat: Float? = null,
        lon: Float? = null
    ): Result<Unit> {
        return try {
            val requestDescription = description.toRequestBody("text/plain".toMediaTypeOrNull())

            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart = MultipartBody.Part.createFormData("photo", imageFile.name, requestImageFile)

            val requestLat = lat?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val requestLon = lon?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.addStory("Bearer $token", requestDescription, imageMultipart, requestLat, requestLon)

            if (!response.error) {
                Result.Success(Unit)
            } else {
                Result.Error(response.message)
            }
        } catch (e: Exception) {
            Result.Error("Gagal menambahkan cerita: ${e.message}")
        }
    }

    fun isLoggedIn(): LiveData<Boolean> {
        return userPreferences.getUserToken()
            .map { !it.isNullOrEmpty() }
            .asLiveData()
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            val token = response.loginResult?.token
            if (!token.isNullOrEmpty()) {
                userPreferences.saveUserToken(token)
                Result.Success(Unit)
            } else {
                Result.Error("Token tidak ditemukan")
            }
        } catch (e: Exception) {
            Result.Error(handleException(e, "Login gagal"))
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<Unit> {
        return try {
            apiService.register(SignUpRequest(name, email, password))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(handleException(e, "Registrasi gagal"))
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            userPreferences.clearUserToken()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Logout gagal: ${e.message}")
        }
    }

    private fun handleException(e: Exception, defaultErrorMessage: String): String {
        return when {
            e.message?.contains("401") == true -> "Akses tidak diizinkan. Periksa kredensial Anda."
            e.message?.contains("timeout", ignoreCase = true) == true -> "Waktu koneksi habis. Periksa jaringan Anda."
            e.message?.contains("Unable to resolve host", ignoreCase = true) == true -> "Tidak dapat terhubung ke server. Periksa koneksi internet Anda."
            else -> "$defaultErrorMessage: ${e.message ?: "Kesalahan tak dikenal"}"
        }
    }
}



