package com.example.appprojek.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appprojek.R
import com.example.appprojek.cart.CartItem

class CartAdapter(private val items: List<CartItem>) : RecyclerView.Adapter<CartAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.itemName)
        val qty: TextView = view.findViewById(R.id.itemQty)
        val subtotal: TextView = view.findViewById(R.id.itemSubtotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.name.text = item.product.name
        holder.qty.text = "x${item.quantity}"
        val subtotal = item.product.priceRupiah * item.quantity
        holder.subtotal.text = "Rp %,d".format(subtotal).replace(',', '.')
    }
}
