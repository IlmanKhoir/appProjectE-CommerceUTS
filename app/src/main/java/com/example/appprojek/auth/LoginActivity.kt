package com.example.appprojek.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appprojek.MainActivity
import com.example.appprojek.databinding.ActivityLoginBinding
import com.example.appprojek.model.LoginRequest
import com.example.appprojek.util.AuthManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val authManager = AuthManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.btnLogin.setOnClickListener { performLogin() }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnGuestLogin.setOnClickListener {
            // Login sebagai guest
            authManager.setGuestMode(true)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnLogin.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE

        lifecycleScope.launch {
           val result = authManager.login(LoginRequest(email, password))
            binding.btnLogin.isEnabled = true
            binding.progressBar.visibility = android.view.View.GONE

            result.onSuccess {
                Toast.makeText(this@LoginActivity, it, Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }.onFailure {
                Toast.makeText(this@LoginActivity, it.message ?: "Gagal login", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
