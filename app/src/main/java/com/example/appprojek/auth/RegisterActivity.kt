package com.example.appprojek.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appprojek.MainActivity
import com.example.appprojek.databinding.ActivityRegisterBinding
import com.example.appprojek.model.RegisterRequest
import com.example.appprojek.util.AuthManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val authManager = AuthManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.btnRegister.setOnClickListener { performRegister() }

        binding.tvLogin.setOnClickListener { finish() }
    }

    private fun performRegister() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (name.isEmpty() ||
                        email.isEmpty() ||
                        phone.isEmpty() ||
                        password.isEmpty() ||
                        confirmPassword.isEmpty()
        ) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Password dan konfirmasi password tidak sama", Toast.LENGTH_SHORT)
                    .show()
            return
        }

        binding.btnRegister.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE

        lifecycleScope.launch {
            val result = authManager.register(RegisterRequest(name, email, phone, password))
            binding.btnRegister.isEnabled = true
            binding.progressBar.visibility = android.view.View.GONE

            result.onSuccess {
                Toast.makeText(this@RegisterActivity, it, Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                finish()
            }.onFailure {
                Toast.makeText(this@RegisterActivity, it.message ?: "Gagal registrasi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
