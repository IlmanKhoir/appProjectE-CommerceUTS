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
            val root = gson.fromJson(body, Map::class.java)
            val orders = (root["orders"] as? List<Map<String, Any?>>).orEmpty()
            return orders.map { row ->
                val itemsRaw = row["items"]
                val items: List<Map<String, Any?>> =
                    when (itemsRaw) {
                        is List<*> -> itemsRaw.filterIsInstance<Map<String, Any?>>()
                        else -> emptyList()
                    }
                OrderRow(
                    id = (row["id"] as Double).toInt(),
                    total_amount = (row["total_amount"] as Double).toInt(),
                    status = row["status"].toString(),
                    order_date = row["order_date"].toString(),
                    shipping_address = row["shipping_address"]?.toString(),
                    payment_method = row["payment_method"]?.toString(),
                    tracking_number = row["tracking_number"]?.toString(),
                    items = items.map { i ->
                        OrderItem(
                            product_id = i["product_id"].toString(),
                            product_name = i["product_name"]?.toString() ?: "",
                            qty = (i["qty"] as Double).toInt(),
                            price = (i["price"] as Double).toInt()
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
