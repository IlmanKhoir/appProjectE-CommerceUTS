package com.example.appprojek.data

import com.example.appprojek.network.ApiClient
import com.google.gson.Gson
import java.io.IOException

class OrderRepository(private val gson: Gson = Gson()) {
    data class OrderItem(val product_id: String, val product_name: String, val qty: Int, val price: Int)
    data class OrderRow(
            val id: Int,
            val total_amount: Int,
            val status: String,
            val order_date: String,
            val shipping_address: String?,
            val payment_method: String?,
            val tracking_number: String?,
            val items: List<OrderItem>
    )

    @Throws(IOException::class)
    fun list(userId: Int): List<OrderRow> {
        val req = ApiClient.buildGet("orders.php?action=list&user_id=$userId")
        ApiClient.httpClient.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            // Try flexible parsing: either { "orders": [...] } or top-level array [...]
            val ordersRaw: List<Map<String, Any?>> = try {
                // First try object with 'orders' field
                val root = try {
                    gson.fromJson(body, Map::class.java)
                } catch (e: Exception) {
                    null
                }
                if (root != null && root.containsKey("orders")) {
                    (root["orders"] as? List<Map<String, Any?>>) ?: emptyList()
                } else {
                    // Try parse body as an array of orders directly
                    try {
                        gson.fromJson(body, List::class.java) as? List<Map<String, Any?>> ?: emptyList()
                    } catch (e: Exception) {
                        // nothing parsed -> include raw body in error for debugging
                        throw IOException("Invalid JSON from server: $body", e)
                    }
                }
            } catch (e: IOException) {
                throw e
            } catch (e: Exception) {
                throw IOException("Invalid JSON from server: $body", e)
            }
            return ordersRaw.map { row ->
                val itemsRaw = row["items"]
                val items: List<Map<String, Any?>> = when (itemsRaw) {
                    is List<*> -> itemsRaw.filterIsInstance<Map<String, Any?>>()
                    else -> emptyList()
                }

                fun toIntAny(v: Any?): Int {
                    return when (v) {
                        is Number -> v.toInt()
                        is String -> v.toDoubleOrNull()?.toInt() ?: 0
                        else -> 0
                    }
                }

                fun toStringAny(v: Any?): String? {
                    return when (v) {
                        null -> null
                        else -> v.toString()
                    }
                }

                OrderRow(
                    id = toIntAny(row["id"]),
                    total_amount = toIntAny(row["total_amount"]),
                    status = toStringAny(row["status"]) ?: "",
                    order_date = toStringAny(row["order_date"]) ?: "",
                    shipping_address = toStringAny(row["shipping_address"]),
                    payment_method = toStringAny(row["payment_method"]),
                    tracking_number = toStringAny(row["tracking_number"]),
                    items = items.map { i ->
                        OrderItem(
                            product_id = toStringAny(i["product_id"]) ?: "",
                            product_name = toStringAny(i["product_name"]) ?: "",
                            qty = toIntAny(i["qty"]),
                            price = toIntAny(i["price"])
                        )
                    }
                )
            }
        }
    }

    @Throws(IOException::class)
    fun create(
            userId: Int,
            totalAmount: Int,
            status: String,
            shippingAddress: String,
            paymentMethod: String,
            trackingNumber: String?,
            items: List<OrderItem>
    ): Int {
        val itemsJson = gson.toJson(items)
        val req =
                ApiClient.buildPostForm(
                        "orders.php?action=create",
                        mapOf(
                                "user_id" to userId.toString(),
                                "total_amount" to totalAmount.toString(),
                                "status" to status,
                                "shipping_address" to shippingAddress,
                                "payment_method" to paymentMethod,
                                "tracking_number" to (trackingNumber ?: ""),
                                "items_json" to itemsJson
                        )
                )
        ApiClient.httpClient.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            val map = gson.fromJson(body, Map::class.java)
            if ((map["success"] as? Boolean) == true) {
                return (map["order_id"] as Double).toInt()
            } else {
                throw IOException(map["error"]?.toString() ?: "Order create failed")
            }
        }
    }
}
