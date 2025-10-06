package com.example.appprojek.util

import android.content.Context
import android.content.SharedPreferences
import com.example.appprojek.model.Review
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ReviewManager(private val context: Context) {
    private val prefs: SharedPreferences =
            context.getSharedPreferences("reviews_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun addReview(review: Review, callback: (Boolean) -> Unit) {
        try {
            val reviews = getReviews().toMutableList()
            reviews.add(review)
            saveReviews(reviews)
            callback(true)
        } catch (e: Exception) {
            callback(false)
        }
    }

    fun getReviews(): List<Review> {
        val reviewsJson = prefs.getString("reviews", null)
        return if (reviewsJson != null) {
            val type = object : TypeToken<List<Review>>() {}.type
            gson.fromJson(reviewsJson, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun getReviewsByProduct(productId: String): List<Review> {
        return getReviews().filter { it.productId == productId }
    }

    fun getAverageRating(productId: String): Float {
        val reviews = getReviewsByProduct(productId)
        return if (reviews.isNotEmpty()) {
            reviews.map { it.rating }.average().toFloat()
        } else {
            0f
        }
    }

    private fun saveReviews(reviews: List<Review>) {
        val reviewsJson = gson.toJson(reviews)
        prefs.edit().putString("reviews", reviewsJson).apply()
    }
}
