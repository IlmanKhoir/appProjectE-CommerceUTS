package com.example.appprojek.model

data class User(
        val id: String,
        val email: String,
        val name: String,
        val phone: String,
        val address: String = "",
        val profileImage: String? = null,
        val joinDate: Long = System.currentTimeMillis()
)

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
        val name: String,
        val email: String,
        val phone: String,
        val password: String
)
