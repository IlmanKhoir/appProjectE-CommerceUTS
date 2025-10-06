package com.example.appprojek.order

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appprojek.TrackingActivity
import com.example.appprojek.databinding.ActivityOrderHistoryBinding
import com.example.appprojek.domain.OrderStatus
import com.example.appprojek.model.CartItem
import com.example.appprojek.model.Order
import com.example.appprojek.model.Product
import com.example.appprojek.ui.OrderAdapter

class OrderHistoryActivity : AppCompatActivity() {
        private lateinit var binding: ActivityOrderHistoryBinding
        private lateinit var orderAdapter: OrderAdapter

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
                setContentView(binding.root)

                setupUI()
                loadOrderHistory()
        }

        private fun setupUI() {
                binding.toolbar.setNavigationOnClickListener { finish() }

                binding.recyclerOrders.layoutManager = LinearLayoutManager(this)
                orderAdapter =
                        OrderAdapter(
                                emptyList(),
                                onOrderClick = { order ->
                                        // Navigate to order detail or tracking
                                        if (order.status == OrderStatus.SHIPPED ||
                                                        order.status == OrderStatus.DELIVERED
                                        ) {
                                                val intent =
                                                        Intent(this, TrackingActivity::class.java)
                                                intent.putExtra("order_id", order.orderId)
                                                intent.putExtra(
                                                        "tracking_number",
                                                        order.trackingNumber
                                                )
                                                startActivity(intent)
                                        } else {
                                                Toast.makeText(
                                                                this,
                                                                "Detail pesanan: ${order.orderId}",
                                                                Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                        }
                                }
                        )
                binding.recyclerOrders.adapter = orderAdapter
        }

        private fun loadOrderHistory() {
                // Simulasi data riwayat pesanan
                val orders =
                        listOf(
                                Order(
                                        orderId = "ORD001",
                                        userId = "user1",
                                        items =
                                                listOf(
                                                        CartItem(
                                                                Product(
                                                                        "p1",
                                                                        "Susu Kotak 1L",
                                                                        23000
                                                                ),
                                                                2
                                                        ),
                                                        CartItem(
                                                                Product("p2", "Roti Tawar", 18000),
                                                                1
                                                        )
                                                ),
                                        totalAmount = 64000,
                                        status = OrderStatus.DELIVERED,
                                        orderDate = System.currentTimeMillis() - 86400000 * 7,
                                        shippingAddress = "Jl. Merdeka No. 123, Jakarta",
                                        paymentMethod = "Transfer Bank",
                                        trackingNumber = "TRK123456789"
                                ),
                                Order(
                                        orderId = "ORD002",
                                        userId = "user1",
                                        items =
                                                listOf(
                                                        CartItem(
                                                                Product(
                                                                        "p3",
                                                                        "Mie Instan Goreng",
                                                                        3500
                                                                ),
                                                                5
                                                        ),
                                                        CartItem(
                                                                Product(
                                                                        "p4",
                                                                        "Minyak Goreng 1L",
                                                                        15500
                                                                ),
                                                                1
                                                        )
                                                ),
                                        totalAmount = 33000,
                                        status = OrderStatus.SHIPPED,
                                        orderDate = System.currentTimeMillis() - 86400000 * 3,
                                        shippingAddress = "Jl. Merdeka No. 123, Jakarta",
                                        paymentMethod = "E-Wallet",
                                        trackingNumber = "TRK987654321"
                                ),
                                Order(
                                        orderId = "ORD003",
                                        userId = "user1",
                                        items =
                                                listOf(
                                                        CartItem(
                                                                Product(
                                                                        "p5",
                                                                        "Beras Premium 5kg",
                                                                        79000
                                                                ),
                                                                1
                                                        )
                                                ),
                                        totalAmount = 79000,
                                        status = OrderStatus.PROCESSING,
                                        orderDate = System.currentTimeMillis() - 86400000,
                                        shippingAddress = "Jl. Merdeka No. 123, Jakarta",
                                        paymentMethod = "Transfer Bank"
                                )
                        )
                orderAdapter.updateOrders(orders)
        }
}
