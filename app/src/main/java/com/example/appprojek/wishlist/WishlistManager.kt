package com.example.appprojek.wishlist

import android.content.Context
import android.content.SharedPreferences
import com.example.appprojek.model.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WishlistManager(private val context: Context) {
    private val prefs: SharedPreferences =
            context.getSharedPreferences("wishlist_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun addToWishlist(product: Product) {
        val wishlist = getWishlist().toMutableList()
        if (!wishlist.any { it.id == product.id }) {
            wishlist.add(product)
            saveWishlist(wishlist)
        }
    }

    fun removeFromWishlist(productId: String) {
        val wishlist = getWishlist().toMutableList()
        wishlist.removeAll { it.id == productId }
        saveWishlist(wishlist)
    }

    fun isInWishlist(productId: String): Boolean {
        return getWishlist().any { it.id == productId }
    }

    fun getWishlist(): List<Product> {
        val wishlistJson = prefs.getString("wishlist", null)
        return if (wishlistJson != null) {
            val type = object : TypeToken<List<Product>>() {}.type
            gson.fromJson(wishlistJson, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun clearWishlist() {
        prefs.edit().remove("wishlist").apply()
    }

    private fun saveWishlist(wishlist: List<Product>) {
        val wishlistJson = gson.toJson(wishlist)
        prefs.edit().putString("wishlist", wishlistJson).apply()
    }
}
