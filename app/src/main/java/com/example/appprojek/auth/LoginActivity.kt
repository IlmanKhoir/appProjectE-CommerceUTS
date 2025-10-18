package com.example.appprojek.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appprojek.MainActivity
import com.example.appprojek.data.UserRepository
import com.example.appprojek.databinding.ActivityLoginBinding
import com.example.appprojek.util.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
            try {
                val repo = UserRepository()
                val result = withContext(Dispatchers.IO) { repo.login(email, password) }
                binding.btnLogin.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE

                if (result.success && result.user_id != null && result.email != null) {
            authManager.saveBackendUser(
                userId = result.user_id ?: -1,
                email = result.email ?: "",
                name = result.name,
                phone = result.phone,
                address = result.address
            )
                    Toast.makeText(this@LoginActivity, "Login berhasil", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                                    this@LoginActivity,
                                    result.error ?: "Gagal login",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }
            } catch (e: Exception) {
                binding.btnLogin.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@LoginActivity, e.message ?: "Gagal login", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }
}
