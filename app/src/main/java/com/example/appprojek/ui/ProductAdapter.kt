package com.example.appprojek.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appprojek.R
import com.example.appprojek.cart.CartManager
import com.example.appprojek.model.Product

class ProductAdapter(
        private var products: List<Product>,
        private val onAddedToCart: (Product) -> Unit,
        private val onProductClick: (Product) -> Unit = {}
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int = products.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
        holder.itemView.setOnClickListener { onProductClick(product) }
        holder.addButton.setOnClickListener {
            CartManager.add(product)
            onAddedToCart(product)
        }
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.productName)
        private val priceText: TextView = itemView.findViewById(R.id.productPrice)
        private val imageView: ImageView = itemView.findViewById(R.id.productImage)
        val addButton: Button = itemView.findViewById(R.id.buttonAdd)

        fun bind(product: Product) {
            nameText.text = product.name
            priceText.text = formatRupiah(product.priceRupiah)
            if (product.imageResId != null) {
                imageView.setImageResource(product.imageResId)
            } else {
                imageView.setImageResource(R.mipmap.ic_launcher)
            }
        }

        private fun formatRupiah(amount: Int): String {
            return "Rp %,d".format(amount).replace(',', '.')
        }
    }

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
