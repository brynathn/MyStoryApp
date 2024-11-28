package com.example.mystoryapp.ui.signup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mystoryapp.AuthViewModelFactory
import com.example.mystoryapp.R
import com.example.mystoryapp.databinding.ActivitySignUpBinding
import com.example.mystoryapp.di.Injection
import com.example.mystoryapp.ui.AuthViewModel
import com.example.mystoryapp.ui.login.LoginActivity
import com.example.mystoryapp.Result


class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val repository = Injection.provideRepository(this)
        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(repository)
        )[AuthViewModel::class.java]

        binding.btnRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            if (validateInput(name, email, password)) {
                authViewModel.register(name, email, password)
            }
        }

        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        authViewModel.registerResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                }
                is Result.Success -> {
                    showLoading(false)
                    Toast.makeText(this, getString(R.string.regist_success), Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Error -> {
                    showLoading(false)
                    Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.all_field), Toast.LENGTH_SHORT).show()
            return false
        }
        if (!binding.edRegisterPassword.validatePassword()) {
            return false
        }
        return true
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingContainer.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
    }
}


