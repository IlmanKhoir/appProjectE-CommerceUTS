package com.example.appprojek.data

import com.example.appprojek.network.ApiClient
import com.google.gson.Gson
import java.io.IOException

class WishlistRepository(private val gson: Gson = Gson()) {

    data class WishlistItem(val product_id: String, val created_at: String)

    @Throws(IOException::class)
    fun list(userId: Int): List<WishlistItem> {
        val req = ApiClient.buildGet("wishlist.php?action=list&user_id=$userId")
        ApiClient.httpClient.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            val root = gson.fromJson(body, Map::class.java)
            val items = (root["items"] as? List<Map<String, Any?>>).orEmpty()
            return items.map {
                WishlistItem(
                        product_id = it["product_id"].toString(),
                        created_at = it["created_at"].toString()
                )
            }
        }
    }

    @Throws(IOException::class)
    fun add(userId: Int, productId: String): Boolean {
        val req =
                ApiClient.buildPostForm(
                        "wishlist.php?action=add",
                        mapOf("user_id" to userId.toString(), "product_id" to productId)
                )
        ApiClient.httpClient.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            val map = gson.fromJson(body, Map::class.java)
            return (map["success"] as? Boolean) == true
        }
    }

    @Throws(IOException::class)
    fun remove(userId: Int, productId: String): Boolean {
        val req =
                ApiClient.buildPostForm(
                        "wishlist.php?action=remove",
                        mapOf("user_id" to userId.toString(), "product_id" to productId)
                )
        ApiClient.httpClient.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            val map = gson.fromJson(body, Map::class.java)
            return (map["success"] as? Boolean) == true
        }
    }
}
