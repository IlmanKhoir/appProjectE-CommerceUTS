package com.example.appprojek.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appprojek.MainActivity
import com.example.appprojek.data.UserRepository
import com.example.appprojek.databinding.ActivityRegisterBinding
import com.example.appprojek.util.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        val address = binding.etAddress.text?.toString()?.trim() ?: ""
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (name.isEmpty() ||
                        email.isEmpty() ||
                        phone.isEmpty() ||
                        password.isEmpty() ||
                        address.isEmpty() ||
                        confirmPassword.isEmpty()
        ) {
            Toast.makeText(this, "Semua field harus diisi (termasuk alamat)", Toast.LENGTH_SHORT)
                    .show()
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
            try {
                val repo = UserRepository()
                Log.d("RegisterActivity", "Register request starting -> email=$email")
                val result =
                        withContext(Dispatchers.IO) {
                            repo.register(email, password, phone, address, name)
                        }
                binding.btnRegister.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE

                if (result.success && result.user_id != null && result.email != null) {
                    Log.d(
                            "RegisterActivity",
                            "Register success user_id=${'$'}{result.user_id} email=${'$'}{result.email}"
                    )
                    // Save user with phone & address into session (server may echo them)
                    authManager.saveBackendUser(
                            result.user_id,
                            result.email,
                            result.phone,
                            result.address
                    )
                    Toast.makeText(this@RegisterActivity, "Registrasi berhasil", Toast.LENGTH_SHORT)
                            .show()
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finish()
                } else {
                    Log.e("RegisterActivity", "Register failed: ${'$'}{result.error}")
                    Toast.makeText(
                                    this@RegisterActivity,
                                    result.error ?: "Gagal registrasi",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }
            } catch (e: Exception) {
                binding.btnRegister.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE
                Log.e("RegisterActivity", "Register exception", e)
                Toast.makeText(
                                this@RegisterActivity,
                                e.message ?: "Gagal registrasi",
                                Toast.LENGTH_SHORT
                        )
                        .show()
            }
        }
    }
}
