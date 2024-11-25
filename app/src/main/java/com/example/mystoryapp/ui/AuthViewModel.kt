package com.example.mystoryapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mystoryapp.data.Repository
import com.example.mystoryapp.Result
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: Repository) : ViewModel() {

    private val _loginState = MutableLiveData<Result<Unit>>()
    val loginState: LiveData<Result<Unit>> = _loginState

    private val _registerResult = MutableLiveData<Result<Unit>>()
    val registerResult: LiveData<Result<Unit>> = _registerResult

    val isLoggedIn: LiveData<Boolean> = authRepository.isLoggedIn()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.postValue(Result.Loading)
            val result = authRepository.login(email, password)
            _loginState.postValue(result)
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerResult.postValue(Result.Loading)
            val result = authRepository.register(name, email, password)
            _registerResult.postValue(result)
        }
    }

    fun logout() {
        viewModelScope.launch {
            _loginState.postValue(Result.Loading)
            val result = authRepository.logout()
            _loginState.postValue(result)
        }
    }
}






