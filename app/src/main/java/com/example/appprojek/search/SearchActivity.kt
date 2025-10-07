package com.example.appprojek.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.appprojek.databinding.ActivitySearchBinding
import com.example.appprojek.model.Product
import com.example.appprojek.product.ProductDetailActivity
import com.example.appprojek.ui.ProductAdapter
import com.example.appprojek.R

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var productAdapter: ProductAdapter
    private val allProducts = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tidak perlu menu search di kanan atas
        setupUI()
        loadProducts()
    }

    private fun setupUI() {
    binding.toolbar.setNavigationOnClickListener { finish() }

    binding.btnSearch.setOnClickListener {
        val query = binding.etSearch.text?.toString() ?: ""
        filterProducts(query)
    }

    binding.btnClear.setOnClickListener { binding.etSearch.text?.clear(); filterProducts("") }

    binding.recyclerProducts.layoutManager = GridLayoutManager(this, 2)
    productAdapter =
        ProductAdapter(
            emptyList(),
            onAddedToCart = { product ->
                Toast.makeText(
                        this,
                        "${product.name} ditambahkan ke keranjang",
                        Toast.LENGTH_SHORT
                    )
                    .show()
            },
            onProductClick = { product ->
                val intent = Intent(this, ProductDetailActivity::class.java)
                intent.putExtra("product", product)
                startActivity(intent)
            }
        )
    binding.recyclerProducts.adapter = productAdapter
    }

    private fun loadProducts() {
        // Simulasi data produk dengan gambar
        allProducts.addAll(
            listOf(
                Product("p1", "Susu Kotak 1L", 23000, imageResId = R.mipmap.ic_launcher, description = "Susu segar dalam kemasan kotak"),
                Product("p2", "Roti Tawar", 18000, imageResId = R.mipmap.ic_launcher, description = "Roti tawar lembut dan segar"),
                Product("p3", "Mie Instan Goreng", 3500, imageResId = R.mipmap.ic_launcher, description = "Mie instan rasa goreng"),
                Product("p4", "Minyak Goreng 1L", 15500, imageResId = R.mipmap.ic_launcher, description = "Minyak goreng berkualitas tinggi"),
                Product("p5", "Beras Premium 5kg", 79000, imageResId = R.mipmap.ic_launcher, description = "Beras premium kualitas terbaik"),
                Product("p6", "Snack Kentang", 12000, imageResId = R.mipmap.ic_launcher, description = "Snack kentang renyah"),
                Product("p7", "Teh Botol", 5000, imageResId = R.mipmap.ic_launcher, description = "Teh botol segar"),
                Product("p8", "Kopi Susu", 8000, imageResId = R.mipmap.ic_launcher, description = "Kopi susu nikmat"),
                Product("p9", "Air Mineral 600ml", 3000, imageResId = R.mipmap.ic_launcher, description = "Air mineral kemasan botol"),
                Product("p10", "Biskuit Coklat", 15000, imageResId = R.mipmap.ic_launcher, description = "Biskuit dengan coklat premium"),
                Product("p11", "Sosis Ayam", 25000, imageResId = R.mipmap.ic_launcher, description = "Sosis ayam berkualitas"),
                Product("p12", "Keju Cheddar", 35000, imageResId = R.mipmap.ic_launcher, description = "Keju cheddar asli"),
                Product("p13", "Yogurt Strawberry", 12000, imageResId = R.mipmap.ic_launcher, description = "Yogurt rasa strawberry"),
                Product("p14", "Cereal Sarapan", 28000, imageResId = R.mipmap.ic_launcher, description = "Cereal untuk sarapan sehat"),
                Product("p15", "Jus Jeruk", 8000, imageResId = R.mipmap.ic_launcher, description = "Jus jeruk segar"),
                Product("p16", "Kerupuk Udang", 18000, imageResId = R.mipmap.ic_launcher, description = "Kerupuk udang renyah")
            )
        )
        productAdapter.updateProducts(allProducts)
    }

    private fun filterProducts(query: String) {
        if (query.isEmpty()) {
            productAdapter.updateProducts(allProducts)
        } else {
            val filteredProducts =
                    allProducts.filter { product ->
                        product.name.contains(query, ignoreCase = true) ||
                                product.description.contains(query, ignoreCase = true)
                    }
            productAdapter.updateProducts(filteredProducts)
        }
    }
}
