package com.example.mystoryapp.retrofit

import com.example.mystoryapp.request.LoginRequest
import com.example.mystoryapp.request.SignUpRequest
import com.example.mystoryapp.response.AddStoryResponse
import com.example.mystoryapp.response.LoginResponse
import com.example.mystoryapp.response.SignUpResponse
import com.example.mystoryapp.response.StoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("register")
    suspend fun register(@Body requestBody: SignUpRequest): SignUpResponse

    @POST("login")
    suspend fun login(@Body requestBody: LoginRequest): LoginResponse

    @GET("stories")
    suspend fun getAllStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = 1,
        @Query("size") size: Int? = 20,
        @Query("location") location: Int? = 0
    ): StoryResponse

    @GET("stories/{id}")
    suspend fun getStoryDetail(
        @Header("Authorization") token: String,
        @Path("id") storyId: String
    ): StoryResponse

    @Multipart
    @POST("stories")
    suspend fun addStory(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part,
        @Part("lat") lat: RequestBody? = null,
        @Part("lon") lon: RequestBody? = null
    ): AddStoryResponse

    @GET("stories")
    suspend fun getStoriesWithLocation(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("location") location : Int = 1,
    ): StoryResponse
}


