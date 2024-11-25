package com.example.mystoryapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mystoryapp.AuthViewModelFactory
import com.example.mystoryapp.databinding.ActivityMainBinding
import com.example.mystoryapp.di.Injection
import com.example.mystoryapp.ui.AuthViewModel
import com.example.mystoryapp.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Gunakan Injection untuk mendapatkan repository dan ViewModel
        val repository = Injection.provideRepository(this)
        authViewModel = ViewModelProvider(this, AuthViewModelFactory(repository))[AuthViewModel::class.java]

        // Amati status login pengguna
        authViewModel.isLoggedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                binding.welcomeMessage.text = "Selamat datang di Aplikasi Story!"
            } else {
                navigateToLogin()
            }
        }

        // Tombol logout
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            Toast.makeText(this@MainActivity, "Berhasil logout", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
