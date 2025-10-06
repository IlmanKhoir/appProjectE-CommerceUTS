package com.example.appprojek.wishlist

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.appprojek.databinding.ActivityWishlistBinding
import com.example.appprojek.product.ProductDetailActivity
import com.example.appprojek.ui.ProductAdapter
import com.example.appprojek.util.AuthManager

class WishlistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWishlistBinding
    private lateinit var productAdapter: ProductAdapter
    private val authManager by lazy { AuthManager(this) }
    private val wishlistManager by lazy {
        val userId = authManager.getCurrentUser()?.id ?: "guest"
        WishlistManager(applicationContext, userId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWishlistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadWishlist()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

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
                            val intent =
                                    android.content.Intent(this, ProductDetailActivity::class.java)
                            intent.putExtra("product", product)
                            startActivity(intent)
                        }
                )
        binding.recyclerProducts.adapter = productAdapter
    }

    private fun loadWishlist() {
        val wishlist = wishlistManager.getWishlist()
        if (wishlist.isEmpty()) {
            binding.tvEmptyWishlist.visibility = android.view.View.VISIBLE
            binding.recyclerProducts.visibility = android.view.View.GONE
        } else {
            binding.tvEmptyWishlist.visibility = android.view.View.GONE
            binding.recyclerProducts.visibility = android.view.View.VISIBLE
            productAdapter.updateProducts(wishlist)
        }
    }

    override fun onResume() {
        super.onResume()
        loadWishlist()
    }
}
