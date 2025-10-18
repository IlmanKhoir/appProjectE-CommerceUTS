package com.example.appprojek.data

import java.io.IOException

class WishlistRepository {

    data class WishlistItem(val product_id: String, val created_at: String)

    @Throws(IOException::class)
    fun list(userId: Int): List<WishlistItem> {
        return DummyDatabase.listWishlist(userId)
    }

    @Throws(IOException::class)
    fun add(userId: Int, productId: String): Boolean {
        return DummyDatabase.addToWishlist(userId, productId)
    }

    @Throws(IOException::class)
    fun remove(userId: Int, productId: String): Boolean {
        return DummyDatabase.removeFromWishlist(userId, productId)
    }
}

