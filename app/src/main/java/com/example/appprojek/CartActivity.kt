package com.example.appprojek.cart

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appprojek.R
import com.example.appprojek.cart.CartManager
import com.example.appprojek.ui.CartAdapter
import com.example.appprojek.voucher.VoucherActivity

class CartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cart)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootCart)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recycler = findViewById<RecyclerView>(R.id.recyclerCart)
        recycler.layoutManager = LinearLayoutManager(this)
        val items = CartManager.getItems()
        recycler.adapter = CartAdapter(items)

        val totalText = findViewById<TextView>(R.id.textTotal)
        totalText.text = "Total: Rp %,d".format(CartManager.getTotalRupiah()).replace(',', '.')

        // Add voucher button click listener
        findViewById<android.widget.Button>(R.id.btnVoucher)?.setOnClickListener {
            startActivity(Intent(this, VoucherActivity::class.java))
        }
    }
}
