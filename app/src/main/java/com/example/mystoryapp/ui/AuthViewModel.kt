package com.example.mystoryapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mystoryapp.data.Repository
import com.example.mystoryapp.AppResult
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: Repository) : ViewModel() {

    private val _loginState = MutableLiveData<AppResult<Unit>>()
    val loginState: LiveData<AppResult<Unit>> = _loginState

    private val _registerAppResult = MutableLiveData<AppResult<Unit>>()
    val registerAppResult: LiveData<AppResult<Unit>> = _registerAppResult

    val isLoggedIn: LiveData<Boolean> = authRepository.isLoggedIn()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.postValue(AppResult.Loading)
            val result = authRepository.login(email, password)
            _loginState.postValue(result)
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerAppResult.postValue(AppResult.Loading)
            val result = authRepository.register(name, email, password)
            _registerAppResult.postValue(result)
        }
    }

    fun logout() {
        viewModelScope.launch {
            _loginState.postValue(AppResult.Loading)
            val result = authRepository.logout()
            _loginState.postValue(result)
        }
    }
}






