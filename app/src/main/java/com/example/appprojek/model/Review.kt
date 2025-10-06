package com.example.appprojek.model

data class Review(
        val id: String,
        val productId: String,
        val userId: String,
        val userName: String,
        val rating: Int, // 1-5
        val comment: String,
        val date: Long = System.currentTimeMillis(),
        val helpful: Int = 0
)
