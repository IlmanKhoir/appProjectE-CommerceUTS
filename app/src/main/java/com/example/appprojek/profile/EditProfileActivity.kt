package com.example.appprojek.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appprojek.R
import com.example.appprojek.data.UserRepository
import com.example.appprojek.util.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_edit_profile)

                val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)
                toolbar.setNavigationOnClickListener { finish() }

                val auth = AuthManager(this)
                val user = auth.getCurrentUser()

                val nameField = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.inputName)
                val phoneField = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.inputPhone)
                val addressField = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.inputAddress)
                val progress = findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.progressBar)
                val layoutPhone = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.layoutPhone)
                val layoutAddress = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.layoutAddress)

                nameField.setText(user?.name ?: "")
                phoneField.setText(user?.phone ?: "")
                addressField.setText(user?.address ?: "")

                findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonSave)
                        .setOnClickListener {
                                val name = nameField.text?.toString()?.trim().orEmpty()
                                val phone = phoneField.text?.toString()?.trim().orEmpty()
                                val address = addressField.text?.toString()?.trim().orEmpty()

                                if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                                        Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                                        return@setOnClickListener
                                }

                                // Validasi sederhana nomor HP (digit 8-15)
                                val phoneDigits = phone.filter { it.isDigit() }
                                if (phoneDigits.length < 8 || phoneDigits.length > 15) {
                                        layoutPhone.error = "Nomor HP tidak valid"
                                        return@setOnClickListener
                                } else layoutPhone.error = null

                                // Validasi alamat tidak terlalu pendek
                                if (address.length < 5) {
                                        layoutAddress.error = "Alamat terlalu pendek"
                                        return@setOnClickListener
                                } else layoutAddress.error = null

                                lifecycleScope.launch {
                                        progress.visibility = android.view.View.VISIBLE
                                        val repo = UserRepository()
                                        val success = withContext(Dispatchers.IO) {
                                                repo.updateProfile(
                                                        userId = user?.id?.toIntOrNull(),
                                                        email = user?.email,
                                                        name = name,
                                                        phone = phone,
                                                        address = address
                                                )
                                        }
                                        if (success) {
                                                // Simpan ke sesi lokal
                                                if (user != null && user.id.isNotEmpty()) {
                                                        AuthManager(this@EditProfileActivity)
                                                                .saveBackendUser(
                                                                        userId = user.id.toInt(),
                                                                        email = user.email,
                                                                        phone = phone,
                                                                        address = address
                                                                )
                                                }
                                                Toast.makeText(this@EditProfileActivity, "Profil diperbarui", Toast.LENGTH_SHORT).show()
                                                setResult(RESULT_OK)
                                                finish()
                                        } else {
                                                Toast.makeText(this@EditProfileActivity, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                                        }
                                        progress.visibility = android.view.View.GONE
                                }
                        }
        }
}


