package com.example.appprojek.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.appprojek.model.LoginRequest
import com.example.appprojek.model.RegisterRequest
import com.example.appprojek.model.User
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class AuthManager(private val context: Context) {
    private val gson = Gson()

    private val prefs by lazy {
        try {
            val masterKey =
                MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            EncryptedSharedPreferences.create(
                context,
                "auth_prefs_secure",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Some devices/emulator images may not support the required keystore features.
            // Fall back to regular SharedPreferences to avoid crash while preserving functionality.
            android.util.Log.w("AuthManager", "EncryptedSharedPreferences unavailable, falling back: ${e.message}")
            context.getSharedPreferences("auth_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    suspend fun login(request: LoginRequest): Result<String> =
            withContext(Dispatchers.IO) {
                try {
                    if (request.email.isEmpty() || request.password.isEmpty()) {
                        return@withContext Result.failure(
                                IllegalArgumentException("Email dan password harus diisi")
                        )
                    }
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(request.email).matches()) {
                        return@withContext Result.failure(
                                IllegalArgumentException("Format email tidak valid")
                        )
                    }

                    // Simulasi network delay
                    delay(1500)

                    return@withContext if (request.email == "user@example.com" &&
                                    request.password == "password123"
                    ) {
                        val user =
                                User(
                                        id = "1",
                                        email = request.email,
                                        name = "User Test",
                                        phone = "081234567890"
                                )
                        saveUser(user)
                        Result.success("Login berhasil")
                    } else {
                        Result.failure(IllegalArgumentException("Email atau password salah"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

    suspend fun register(request: RegisterRequest): Result<String> =
            withContext(Dispatchers.IO) {
                try {
                    if (request.name.isEmpty() ||
                                    request.email.isEmpty() ||
                                    request.phone.isEmpty() ||
                                    request.password.isEmpty()
                    ) {
                        return@withContext Result.failure(
                                IllegalArgumentException("Semua field harus diisi")
                        )
                    }
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(request.email).matches()) {
                        return@withContext Result.failure(
                                IllegalArgumentException("Format email tidak valid")
                        )
                    }
                    if (request.password.length < 6) {
                        return@withContext Result.failure(
                                IllegalArgumentException("Password minimal 6 karakter")
                        )
                    }

                    delay(1500)

                    val user =
                            User(
                                    id = System.currentTimeMillis().toString(),
                                    email = request.email,
                                    name = request.name,
                                    phone = request.phone
                            )
                    saveUser(user)
                    Result.success("Registrasi berhasil")
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getString("user_data", null) != null
    }

    fun isGuestMode(): Boolean {
        return prefs.getBoolean("guest_mode", false)
    }

    fun setGuestMode(isGuest: Boolean) {
        prefs.edit().putBoolean("guest_mode", isGuest).apply()
    }

    fun getCurrentUser(): User? {
        val userJson = prefs.getString("user_data", null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else null
    }

    private fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        prefs.edit().putString("user_data", userJson).putBoolean("guest_mode", false).apply()
    }

    // Save user from backend login/register, with name, phone, address
    fun saveBackendUser(
        userId: Int,
        email: String,
        name: String? = null,
        phone: String? = null,
        address: String? = null
    ) {
        val user = User(
            id = userId.toString(),
            email = email,
            name = name ?: "",
            phone = phone ?: "",
            address = address ?: ""
        )
        saveUser(user)
    }
}
