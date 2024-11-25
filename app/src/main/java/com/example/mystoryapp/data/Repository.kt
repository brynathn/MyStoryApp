package com.example.mystoryapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.mystoryapp.UserPreferences
import com.example.mystoryapp.request.LoginRequest
import com.example.mystoryapp.request.SignUpRequest
import com.example.mystoryapp.retrofit.ApiService
import kotlinx.coroutines.flow.map
import com.example.mystoryapp.Result

class Repository(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences
) {

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



