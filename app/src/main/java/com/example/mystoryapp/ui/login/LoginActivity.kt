package com.example.mystoryapp.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mystoryapp.AuthViewModelFactory
import com.example.mystoryapp.R
import com.example.mystoryapp.databinding.ActivityLoginBinding
import com.example.mystoryapp.ui.AuthViewModel
import com.example.mystoryapp.ui.main.MainActivity
import com.example.mystoryapp.ui.signup.SignUpActivity
import com.example.mystoryapp.Result
import com.example.mystoryapp.di.Injection
import com.example.mystoryapp.ui.widget.MyStoryWidget


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        setupViewModel()
        setupUI()
        observeViewModel()
        playAnimation()
    }

    private fun setupViewModel() {
        val repository = Injection.provideRepository(this)
        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(repository)
        )[AuthViewModel::class.java]

        authViewModel.isLoggedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                navigateToMainActivity()
            }
        }
    }


    private fun setupUI() {
        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            if (validateInput(email, password)) {
                authViewModel.login(email, password)
            }
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun observeViewModel() {
        authViewModel.loginState.observe(this) { state ->
            when (state) {
                is Result.Loading -> {
                    showLoading(true)
                }
                is Result.Success -> {
                    showLoading(false)
                    Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                    MyStoryWidget().refreshWidget(this)
                    navigateToMainActivity()
                }
                is Result.Error -> {
                    showLoading(false)
                    Toast.makeText(this, state.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.email_password_empty), Toast.LENGTH_SHORT).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edLoginEmail.error = getString(R.string.invalid_email_format)
            return false
        }
        if (!binding.edLoginPassword.validatePassword()) {
            return false
        }
        return true
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val emailTextView =
            ObjectAnimator.ofFloat(binding.tvLoginEmail, View.ALPHA, 1f).setDuration(100)
        val emailEditTextLayout =
            ObjectAnimator.ofFloat(binding.tilLoginEmail, View.ALPHA, 1f).setDuration(100)
        val passwordTextView =
            ObjectAnimator.ofFloat(binding.tvLoginPassword, View.ALPHA, 1f).setDuration(100)
        val passwordEditTextLayout =
            ObjectAnimator.ofFloat(binding.tilLoginPassword, View.ALPHA, 1f).setDuration(100)
        val registerLinkTextView =
            ObjectAnimator.ofFloat(binding.tvRegisterLink, View.ALPHA, 1f).setDuration(100)
        val login = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(
                emailTextView,
                emailEditTextLayout,
                passwordTextView,
                passwordEditTextLayout,
                registerLinkTextView,
                login
            )
            startDelay = 100
        }.start()
    }
}
