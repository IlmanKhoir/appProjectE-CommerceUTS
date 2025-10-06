package com.example.appprojek

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class ShippingActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_shipping)

                val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
                toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

                // Navigate to tracking activity when user wants to track
                // This is a simple activity that redirects to the tracking page
                val orderId = intent.getStringExtra("order_id")
                val i = Intent(this, com.example.appprojek.shipping.ShippingTrackingActivity::class.java)
                if (orderId != null) i.putExtra("order_id", orderId)
                startActivity(i)
                finish()
        }
}
