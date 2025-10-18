package com.example.appprojek.data

import java.io.IOException

class OrderRepository(private val gson: com.google.gson.Gson = com.google.gson.Gson()) {
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
        return DummyDatabase.listOrders(userId)
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
        return DummyDatabase.createOrder(userId, totalAmount, status, shippingAddress, paymentMethod, trackingNumber, items)
    }
}
