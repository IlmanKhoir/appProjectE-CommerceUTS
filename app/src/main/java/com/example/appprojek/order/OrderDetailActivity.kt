package com.example.appprojek.order

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appprojek.R
import com.example.appprojek.model.Order

class OrderDetailActivity : AppCompatActivity() {
        private var order: Order? = null

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_order_detail)

                val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)
                toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

                order = intent.getParcelableExtra("order")

                render()

                findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonTrack)
                        .setOnClickListener {
                                val o = order ?: return@setOnClickListener
                                val intent =
                                        android.content.Intent(
                                                this,
                                                com.example.appprojek.shipping.ShippingTrackingActivity::class.java
                                        )
                                intent.putExtra("order_id", o.orderId)
                                startActivity(intent)
                        }
        }

        private fun render() {
                val o = order ?: return
                findViewById<android.widget.TextView>(R.id.tvOrderId).text = "#${'$'}{o.orderId}"
                findViewById<android.widget.TextView>(R.id.tvStatus).text = o.status.name
                findViewById<android.widget.TextView>(R.id.tvAddress).text = o.shippingAddress
                findViewById<android.widget.TextView>(R.id.tvPayment).text = o.paymentMethod

                val list = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerItems)
                list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
                list.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<ItemVH>() {
                        private val items = o.items
                        override fun onCreateViewHolder(
                                parent: android.view.ViewGroup,
                                viewType: Int
                        ): ItemVH {
                                val v = layoutInflater.inflate(R.layout.item_order_detail_product, parent, false)
                                return ItemVH(v)
                        }
                        override fun getItemCount(): Int = items.size
                        override fun onBindViewHolder(holder: ItemVH, position: Int) = holder.bind(items[position])
                }
        }

        private class ItemVH(itemView: android.view.View) :
                androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
                fun bind(ci: com.example.appprojek.model.CartItem) {
                        itemView.findViewById<android.widget.TextView>(R.id.tvName).text = ci.product.name
                        itemView.findViewById<android.widget.TextView>(R.id.tvQty).text = "x${'$'}{ci.quantity}"
                        itemView.findViewById<android.widget.TextView>(R.id.tvPrice).text =
                                "Rp %,d".format(ci.product.priceRupiah).replace(',', '.')
                }
        }
}


