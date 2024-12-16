package com.example.mystoryapp.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.mystoryapp.R
import com.example.mystoryapp.UserPreferences
import com.example.mystoryapp.request.LoginRequest
import com.example.mystoryapp.request.SignUpRequest
import com.example.mystoryapp.retrofit.ApiService
import kotlinx.coroutines.flow.map
import com.example.mystoryapp.AppResult
import com.example.mystoryapp.database.StoryDatabase
import com.example.mystoryapp.response.StoryItem
import com.example.mystoryapp.ui.main.StoryRemoteMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class Repository(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences,
    private val context: Context,
    private val storyDatabase: StoryDatabase
) {
    suspend fun getUserToken(): String? {
        return userPreferences.getUserToken().firstOrNull()
    }

    suspend fun getStoryCount(): Int {
        return withContext(Dispatchers.IO) {
            storyDatabase.storyDao().getStoryCount()
        }
    }

    suspend fun getAllStories(token: String): AppResult<List<StoryItem>> {
        return try {
            val response = apiService.getAllStories("Bearer $token")
            if (!response.error) {
                AppResult.Success(response.listStory ?: emptyList())
            } else {
                AppResult.Error(response.message)
            }
        } catch (e: Exception) {
            AppResult.Error(context.getString(R.string.error_load_stories, e.message))
        }
    }

    suspend fun getStoryDetail(token: String, storyId: String): AppResult<StoryItem> {
        return try {
            val response = apiService.getStoryDetail("Bearer $token", storyId)
            if (!response.error && response.story != null) {
                AppResult.Success(response.story)
            } else {
                AppResult.Error(response.message)
            }
        } catch (e: Exception) {
            AppResult.Error(context.getString(R.string.error_load_story_detail, e.message))
        }
    }

    suspend fun getStoriesWithLocation(token: String): AppResult<List<StoryItem>> {
        return try {
            val response = apiService.getStoriesWithLocation("Bearer $token")
            if (!response.error) {
                AppResult.Success(response.listStory!!.filter { it.lat != null && it.lon != null })
            } else {
                AppResult.Error(response.message)
            }
        } catch (e: Exception) {
            AppResult.Error(context.getString(R.string.error_load_stories, e.message))
        }
    }

    fun getStoriesPagingData(token: String): LiveData<PagingData<StoryItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            remoteMediator = StoryRemoteMediator(apiService, storyDatabase, token),
            pagingSourceFactory = {
//                StoryPagingSource(apiService, token)
                storyDatabase.storyDao().getStories()
            }
        ).liveData
    }

    suspend fun addStory(
        description: String,
        imageFile: File,
        token: String,
        lat: Float? = null,
        lon: Float? = null
    ): AppResult<Unit> {
        return try {
            val requestDescription = description.toRequestBody("text/plain".toMediaTypeOrNull())

            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart = MultipartBody.Part.createFormData("photo", imageFile.name, requestImageFile)

            val requestLat = lat?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val requestLon = lon?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.addStory("Bearer $token", requestDescription, imageMultipart, requestLat, requestLon)

            if (!response.error) {
                AppResult.Success(Unit)
            } else {
                AppResult.Error(response.message)
            }
        } catch (e: Exception) {
            AppResult.Error(context.getString(R.string.error_add_story, e.message))
        }
    }

    fun isLoggedIn(): LiveData<Boolean> {
        return userPreferences.getUserToken()
            .map { !it.isNullOrEmpty() }
            .asLiveData()
    }

    suspend fun login(email: String, password: String): AppResult<Unit> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            val token = response.loginResult?.token
            if (!token.isNullOrEmpty()) {
                userPreferences.saveUserToken(token)
                AppResult.Success(Unit)
            } else {
                AppResult.Error(context.getString(R.string.error_no_token))
            }
        } catch (e: Exception) {
            AppResult.Error(context.getString(R.string.error_login, handleException(e)))
        }
    }

    suspend fun register(name: String, email: String, password: String): AppResult<Unit> {
        return try {
            apiService.register(SignUpRequest(name, email, password))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(context.getString(R.string.error_register, handleException(e)))
        }
    }

    suspend fun logout(): AppResult<Unit> {
        return try {
            userPreferences.clearUserToken()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(context.getString(R.string.error_logout, e.message))
        }
    }

    private fun handleException(e: Exception): String {
        return when {
            e.message?.contains("401") == true -> context.getString(R.string.error_unauthorized)
            e.message?.contains("timeout", ignoreCase = true) == true ->  context.getString(R.string.error_timeout)
            e.message?.contains("Unable to resolve host", ignoreCase = true) == true -> context.getString(R.string.error_no_connection)
            else -> context.getString(R.string.error_unknown)
        }
    }
}



