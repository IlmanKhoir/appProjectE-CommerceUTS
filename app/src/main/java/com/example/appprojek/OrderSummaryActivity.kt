package com.example.appprojek.order

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appprojek.R
import com.example.appprojek.data.OrderRepository
import com.example.appprojek.util.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderSummaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_summary)

        val toolbar =
                findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val total = intent.getStringExtra("totalPay") ?: "Rp 0"
        val method = intent.getStringExtra("method") ?: "-"
        findViewById<TextView>(R.id.textMethod).text = method
        findViewById<TextView>(R.id.textTotal).text = total

        val layoutVa = findViewById<android.view.View>(R.id.layoutVa)
        val textVa = findViewById<TextView>(R.id.textVa)
        val buttonCopy = findViewById<Button>(R.id.buttonCopyVa)
        if (method.contains("Transfer", ignoreCase = true)) {
            layoutVa.visibility = android.view.View.VISIBLE
            val va = generateRandomVa()
            textVa.text = va
            buttonCopy.setOnClickListener {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("VA", va))
                android.widget.Toast.makeText(
                                this,
                                getString(R.string.copied),
                                android.widget.Toast.LENGTH_SHORT
                        )
                        .show()
            }
        } else {
            layoutVa.visibility = android.view.View.GONE
        }

        findViewById<Button>(R.id.buttonConfirm).setOnClickListener {
            val auth = AuthManager(this)
            val currentUser = auth.getCurrentUser()
            if (currentUser == null) {
                android.widget.Toast.makeText(
                                this,
                                "Silakan login terlebih dahulu",
                                android.widget.Toast.LENGTH_SHORT
                        )
                        .show()
                return@setOnClickListener
            }

            val userId = currentUser.id.toIntOrNull()
            if (userId == null) {
                android.widget.Toast.makeText(
                                this,
                                "User ID tidak valid",
                                android.widget.Toast.LENGTH_SHORT
                        )
                        .show()
                return@setOnClickListener
            }

            val totalNumeric = intent.getIntExtra("totalAmountInt", 0)
            val address = intent.getStringExtra("shippingAddress") ?: ""
            val methodVal = method

            if (totalNumeric <= 0) {
                android.widget.Toast.makeText(
                                this,
                                "Total tidak valid",
                                android.widget.Toast.LENGTH_SHORT
                        )
                        .show()
                return@setOnClickListener
            }
            if (address.isBlank()) {
                android.widget.Toast.makeText(
                                this,
                                "Alamat pengiriman wajib diisi",
                                android.widget.Toast.LENGTH_SHORT
                        )
                        .show()
                return@setOnClickListener
            }
            if (methodVal.isBlank()) {
                android.widget.Toast.makeText(
                                this,
                                "Metode pembayaran wajib diisi",
                                android.widget.Toast.LENGTH_SHORT
                        )
                        .show()
                return@setOnClickListener
            }

            val repo = OrderRepository()
            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        repo.create(
                                userId = userId,
                                totalAmount = totalNumeric,
                                status = "paid",
                                shippingAddress = address,
                                paymentMethod = methodVal,
                                trackingNumber = null,
                                items = com.example.appprojek.domain.CartServiceAdapter().getItems().map { it ->
                                    com.example.appprojek.data.OrderRepository.OrderItem(
                                            product_id = it.product.id,
                                            qty = it.quantity,
                                            price = it.product.priceRupiah
                                    )
                                }
                        )
                    }
                    android.widget.Toast.makeText(
                                    this@OrderSummaryActivity,
                                    "Pesanan dibuat",
                                    android.widget.Toast.LENGTH_SHORT
                            )
                            .show()
                    // Bersihkan keranjang setelah order berhasil
                    com.example.appprojek.domain.CartServiceAdapter().clear()

                    startActivity(
                            android.content.Intent(
                                    this@OrderSummaryActivity,
                                    com.example.appprojek.order.OrderHistoryActivity::class.java
                            )
                    )
                    finish()
                } catch (e: Exception) {
                    android.widget.Toast.makeText(
                                    this@OrderSummaryActivity,
                                    e.message ?: "Gagal membuat pesanan",
                                    android.widget.Toast.LENGTH_LONG
                            )
                            .show()
                }
            }
        }

        // Tambah tombol Lacak Pengiriman untuk langsung melihat map animasi driver
        findViewById<Button>(R.id.buttonTrack).setOnClickListener {
            startActivity(
                    android.content.Intent(
                            this@OrderSummaryActivity,
                            com.example.appprojek.shipping.ShippingTrackingActivity::class.java
                    )
            )
        }

        val textCountdown = findViewById<TextView>(R.id.textCountdown)
        object : CountDownTimer(15 * 60 * 1000L, 1000L) {
                    override fun onTick(millisUntilFinished: Long) {
                        val m = (millisUntilFinished / 1000) / 60
                        val s = (millisUntilFinished / 1000) % 60
                        textCountdown.text = String.format("Sisa waktu bayar: %02d:%02d", m, s)
                    }
                    override fun onFinish() {
                        textCountdown.text = "Waktu pembayaran habis"
                    }
                }
                .start()
    }

    private fun generateRandomVa(): String {
        val prefix = "8808"
        val rand = (100000000..999999999).random()
        return prefix + rand.toString()
    }
}
