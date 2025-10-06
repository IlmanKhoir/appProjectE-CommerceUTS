package com.example.appprojek.product

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appprojek.R
import com.example.appprojek.cart.CartManager
import com.example.appprojek.databinding.ActivityProductDetailBinding
import com.example.appprojek.model.Product
import com.example.appprojek.model.Review
import com.example.appprojek.review.WriteReviewActivity
import com.example.appprojek.ui.ReviewAdapter
import com.example.appprojek.util.ReviewManager
import com.example.appprojek.wishlist.WishlistManager

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var reviewAdapter: ReviewAdapter
    private val wishlistManager by lazy { WishlistManager(applicationContext) }
    private val reviewManager by lazy { ReviewManager(applicationContext) }
    private var product: Product? = null
    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // PERBAIKAN 1: Compatible getParcelableExtra
        product =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra("product", Product::class.java)
                } else {
                    @Suppress("DEPRECATION") intent.getParcelableExtra("product")
                }

        setupUI()
        loadProductData()
        loadReviews()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnAddToCart.setOnClickListener { addToCart() }

        binding.btnDecrease.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.tvQuantity.text = quantity.toString()
            }
        }

        binding.btnIncrease.setOnClickListener {
            quantity++
            binding.tvQuantity.text = quantity.toString()
        }

        binding.btnWriteReview.setOnClickListener {
            product?.let { product ->
                val intent = Intent(this, WriteReviewActivity::class.java)
                intent.putExtra("product", product)
                startActivity(intent)
            }
        }

        binding.btnAddToWishlist.setOnClickListener {
            product?.let { product ->
                if (wishlistManager.isInWishlist(product.id)) {
                    wishlistManager.removeFromWishlist(product.id)
                    binding.btnAddToWishlist.text = "Tambah ke Wishlist"
                    Toast.makeText(this, "Dihapus dari wishlist", Toast.LENGTH_SHORT).show()
                } else {
                    wishlistManager.addToWishlist(product)
                    binding.btnAddToWishlist.text = "Hapus dari Wishlist"
                    Toast.makeText(this, "Ditambahkan ke wishlist", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Setup reviews RecyclerView
        binding.recyclerReviews.layoutManager = LinearLayoutManager(this)
        reviewAdapter = ReviewAdapter(emptyList())
        binding.recyclerReviews.adapter = reviewAdapter
    }

    private fun loadProductData() {
        product?.let { product ->
            binding.toolbar.title = product.name
            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = formatRupiah(product.priceRupiah)
            binding.tvProductDescription.text = product.description
            binding.tvQuantity.text = quantity.toString()

            if (product.imageResId != null) {
                binding.ivProductImage.setImageResource(product.imageResId)
            } else {
                binding.ivProductImage.setImageResource(R.mipmap.ic_launcher)
            }

            // Simulasi rating dan review count
            binding.ratingBar.rating = 4.2f
            binding.tvRatingCount.text = "(128 review)"

            // Update wishlist button
            if (wishlistManager.isInWishlist(product.id)) {
                binding.btnAddToWishlist.text = "Hapus dari Wishlist"
            } else {
                binding.btnAddToWishlist.text = "Tambah ke Wishlist"
            }
        }
                ?: run {
                    // Handle jika product null
                    Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                }
    }

    private fun loadReviews() {
        product?.let { product ->
            val reviews = reviewManager.getReviewsByProduct(product.id)
            if (reviews.isEmpty()) {
                // Load sample reviews if no reviews exist
                val sampleReviews =
                        listOf(
                                Review(
                                        "r1",
                                        product.id,
                                        "u1",
                                        "Ahmad S.",
                                        5,
                                        "Produk berkualitas bagus, sesuai dengan deskripsi. Pengiriman cepat!",
                                        System.currentTimeMillis() - 86400000,
                                        12
                                ),
                                Review(
                                        "r2",
                                        product.id,
                                        "u2",
                                        "Siti M.",
                                        4,
                                        "Bagus, tapi harganya agak mahal. Overall puas dengan kualitas.",
                                        System.currentTimeMillis() - 172800000,
                                        8
                                ),
                                Review(
                                        "r3",
                                        product.id,
                                        "u3",
                                        "Budi K.",
                                        5,
                                        "Sangat puas! Produk original dan pengiriman super cepat. Recommended!",
                                        System.currentTimeMillis() - 259200000,
                                        15
                                )
                        )
                reviewAdapter.updateReviews(sampleReviews)
            } else {
                reviewAdapter.updateReviews(reviews)
            }
        }
    }

    private fun addToCart() {
        product?.let { product ->
            for (i in 1..quantity) {
                CartManager.add(product)
            }
            Toast.makeText(
                            this,
                            "$quantity ${product.name} ditambahkan ke keranjang",
                            Toast.LENGTH_SHORT
                    )
                    .show()
        }
    }

    // PERBAIKAN 2: Format Rupiah yang lebih aman
    private fun formatRupiah(amount: Int): String {
        return try {
            "Rp %,d".format(amount).replace(',', '.')
        } catch (e: Exception) {
            "Rp $amount"
        }
    }
}
