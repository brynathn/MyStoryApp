package com.example.mystoryapp

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val errorMessage: String) : AppResult<Nothing>()
    data object Loading : AppResult<Nothing>()
}
