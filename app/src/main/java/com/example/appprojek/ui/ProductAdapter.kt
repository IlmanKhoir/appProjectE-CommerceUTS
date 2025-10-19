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

    // optional highlight query; adapter consumer should set this when filtering
    private var highlightQuery: String? = null

        /*
            Catatan singkat:
            - Adapter ini yang nampilin item produk di grid.
            - Fungsi `setHighlightQuery` dipake untuk nge-bold + nge-color nama produk yang cocok sama kata pencarian.
            - Kalau gambar nggak muncul, cek resource id di Product.imageResId (default pake ic_launcher).
            - Debug cepat: tambahin Log.d("ProductAdapter", "bind: ${product.name}") di bind()
        */

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

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.productName)
        private val priceText: TextView = itemView.findViewById(R.id.productPrice)
        private val imageView: ImageView = itemView.findViewById(R.id.productImage)
        val addButton: Button = itemView.findViewById(R.id.buttonAdd)

        fun bind(product: Product) {
            // apply highlight using adapter state
            applyHighlight(nameText, product.name)
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

    fun setHighlightQuery(query: String?) {
        highlightQuery = query
        notifyDataSetChanged()
    }

    private fun applyHighlight(textView: TextView, text: String) {
        val q = highlightQuery
        if (q.isNullOrBlank()) {
            textView.text = text
            return
        }
        val start = text.indexOf(q, ignoreCase = true)
        if (start < 0) {
            textView.text = text
            return
        }
        val end = start + q.length
        val spannable = android.text.SpannableString(text)
        val color = android.graphics.Color.parseColor("#D32F2F") // accent highlight
        spannable.setSpan(android.text.style.ForegroundColorSpan(color), start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannable
    }
}
