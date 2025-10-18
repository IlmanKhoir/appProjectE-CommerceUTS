package com.example.appprojek.data

class UserRepository {

    data class LoginResult(
            val success: Boolean,
            val user_id: Int?,
            val email: String?,
            val name: String?,
            val phone: String?,
            val address: String?,
            val error: String?
    )

    fun register(
            email: String,
            password: String,
            phone: String? = null,
            address: String? = null,
            name: String? = null
    ): LoginResult {
        val id = DummyDatabase.register(email, password, name, phone, address)
        return if (id != null) {
            LoginResult(true, id, email, name, phone, address, null)
        } else {
            LoginResult(false, null, email, null, null, null, "Email already registered")
        }
    }

    fun login(email: String, password: String): LoginResult {
        val id = DummyDatabase.login(email, password)
        return if (id != null) {
            val profile = DummyDatabase.getProfileByEmail(email)
            LoginResult(true, id, email, profile?.get("name"), profile?.get("phone"), profile?.get("address"), null)
        } else {
            LoginResult(false, null, email, null, null, null, "Invalid credentials")
        }
    }

    fun getProfileByEmail(email: String): LoginResult {
        val profile = DummyDatabase.getProfileByEmail(email)
        return if (profile != null) {
            LoginResult(true, profile["user_id"]?.toIntOrNull(), profile["email"], profile["name"], profile["phone"], profile["address"], null)
        } else {
            LoginResult(false, null, email, null, null, null, "Not found")
        }
    }

    fun updateProfile(
            userId: Int?,
            email: String?,
            name: String?,
            phone: String?,
            address: String?,
            latitude: Double? = null,
            longitude: Double? = null
    ): Boolean {
        return DummyDatabase.updateProfile(userId, email, name, phone, address, latitude, longitude)
    }
}
