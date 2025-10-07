package com.example.appprojek.order

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appprojek.shipping.ShippingTrackingActivity
import com.example.appprojek.data.OrderRepository
import com.example.appprojek.databinding.ActivityOrderHistoryBinding
import com.example.appprojek.domain.OrderStatus
import com.example.appprojek.model.CartItem
import com.example.appprojek.model.Order
import com.example.appprojek.model.Product
import com.example.appprojek.ui.OrderAdapter
import com.example.appprojek.util.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrderHistoryActivity : AppCompatActivity() {
        private lateinit var binding: ActivityOrderHistoryBinding
        private lateinit var orderAdapter: OrderAdapter
        private val orderRepository by lazy { OrderRepository() }
        private val authManager by lazy { AuthManager(this) }
        private var currentOrders: MutableList<Order> = mutableListOf()
        private val prefs by lazy { getSharedPreferences("order_progress_prefs", MODE_PRIVATE) }

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
                                        val intent = Intent(this, OrderDetailActivity::class.java)
                                        intent.putExtra("order", order)
                                        startActivity(intent)
                                }
                        )
                binding.recyclerOrders.adapter = orderAdapter
        }

        private fun loadOrderHistory() {
                val user = authManager.getCurrentUser()
                val userId = user?.id?.toIntOrNull()
                if (userId == null) {
                        Toast.makeText(
                                        this,
                                        "Silakan login untuk melihat riwayat pesanan",
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                        orderAdapter.updateOrders(emptyList())
                        return
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val rows = orderRepository.list(userId)
                        val mapped = rows.map { r ->
                            Order(
                                orderId = r.id.toString(),
                                userId = userId.toString(),
                                items = r.items.map { i ->
                                    CartItem(
                                        Product(
                                            i.product_id,
                                            i.product_name,
                                            i.price
                                        ),
                                        i.qty
                                    )
                                },
                                totalAmount = r.total_amount,
                                status = when (r.status.lowercase()) {
                                    "delivered" -> OrderStatus.DELIVERED
                                    "shipped", "in_transit" -> OrderStatus.SHIPPED
                                    else -> OrderStatus.PROCESSING
                                },
                                orderDate = System.currentTimeMillis(),
                                shippingAddress = r.shipping_address ?: "",
                                paymentMethod = r.payment_method ?: "",
                                trackingNumber = r.tracking_number
                            )
                        }
                        runOnUiThread {
                            currentOrders = mapped.toMutableList()
                            // Restore persisted status per order id
                            currentOrders = currentOrders.map { o ->
                                val saved = prefs.getString("status_${'$'}{o.orderId}", null)
                                if (saved != null) o.copy(status = OrderStatus.valueOf(saved)) else o
                            }.toMutableList()
                            orderAdapter.updateOrders(currentOrders)
                            simulateStatusProgress()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(
                                this@OrderHistoryActivity,
                                e.message ?: "Gagal memuat pesanan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        }

        private fun simulateStatusProgress() {
                // Ubah status setiap 2 detik: PROCESSING -> SHIPPED -> DELIVERED
                lifecycleScope.launch {
                        try {
                                // Step 1: pastikan minimal PROCESSING
                                currentOrders = currentOrders.map { o ->
                                        if (o.status == OrderStatus.PENDING || o.status == OrderStatus.CONFIRMED)
                                                o.copy(status = OrderStatus.PROCESSING) else o
                                }.toMutableList()
                                orderAdapter.updateOrders(currentOrders)
                                persistStatuses()

                                kotlinx.coroutines.delay(2000)

                                // Step 2: SHIPPED (sedang dalam perjalanan)
                                currentOrders = currentOrders.map { o ->
                                        if (o.status == OrderStatus.PROCESSING) o.copy(status = OrderStatus.SHIPPED)
                                        else o
                                }.toMutableList()
                                orderAdapter.updateOrders(currentOrders)
                                persistStatuses()

                                kotlinx.coroutines.delay(2000)

                                // Step 3: DELIVERED (paket telah sampai tujuan)
                                currentOrders = currentOrders.map { o ->
                                        if (o.status == OrderStatus.SHIPPED) o.copy(status = OrderStatus.DELIVERED)
                                        else o
                                }.toMutableList()
                                orderAdapter.updateOrders(currentOrders)
                                persistStatuses()
                        } catch (_: Exception) { }
                }
        }

        private fun persistStatuses() {
                val e = prefs.edit()
                currentOrders.forEach { o -> e.putString("status_${'$'}{o.orderId}", o.status.name) }
                e.apply()
        }
}
