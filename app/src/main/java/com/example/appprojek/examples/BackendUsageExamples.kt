package com.example.appprojek.examples

import android.util.Log
import com.example.appprojek.data.OrderRepository
import com.example.appprojek.data.UserRepository
import com.example.appprojek.data.WishlistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BackendUsageExamples {
    suspend fun demoFlow() =
            withContext(Dispatchers.IO) {
                val userRepo = UserRepository()
                val wishlistRepo = WishlistRepository()
                val orderRepo = OrderRepository()

                val reg = userRepo.register("user@example.com", "password123")
                Log.d("BackendDemo", "register: $reg")
                val login = userRepo.login("user@example.com", "password123")
                Log.d("BackendDemo", "login: $login")
                val userId = login.user_id ?: return@withContext

                wishlistRepo.add(userId, "p1")
                val wl = wishlistRepo.list(userId)
                Log.d("BackendDemo", "wishlist: $wl")

                val orderId =
                        orderRepo.create(
                                userId = userId,
                                totalAmount = 50000,
                                status = "pending",
                                shippingAddress = "Jl. Contoh No. 1",
                                paymentMethod = "COD",
                                trackingNumber = null,
                                items = listOf(OrderRepository.OrderItem("p1", 2, 25000))
                        )
                Log.d("BackendDemo", "order created: $orderId")
                val orders = orderRepo.list(userId)
                Log.d("BackendDemo", "orders: $orders")
            }
}
