package com.example.appprojek.review

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appprojek.databinding.ActivityWriteReviewBinding
import com.example.appprojek.R
import com.example.appprojek.model.Product
import com.example.appprojek.model.Review
import com.example.appprojek.util.ReviewManager

class WriteReviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWriteReviewBinding
    private lateinit var product: Product
    private val reviewManager = ReviewManager(this)
    private var rating = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        product = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("product", Product::class.java)
        } else {
            @Suppress("DEPRECATION") intent.getParcelableExtra("product")
        } ?: return
        setupUI()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.title = "Tulis Review"

        // Setup rating stars
        setupRatingStars()

        // Setup submit button
        binding.btnSubmit.setOnClickListener { submitReview() }
    }

    private fun setupRatingStars() {
        val stars =
                listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)

        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                rating = index + 1
                updateStarDisplay(stars)
            }
        }
    }

    private fun updateStarDisplay(stars: List<android.widget.ImageView>) {
        stars.forEachIndexed { index, star ->
            star.setImageResource(
                    if (index < rating) R.drawable.ic_star_filled else R.drawable.ic_star_empty
            )
        }
    }

    private fun submitReview() {
        val reviewText = binding.etReview.text.toString().trim()

        if (rating == 0) {
            Toast.makeText(this, "Berikan rating terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (reviewText.isEmpty()) {
            Toast.makeText(this, "Tulis review terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val review =
                Review(
                        id = System.currentTimeMillis().toString(),
                        productId = product.id,
                        userId = "user1", // In real app, get from AuthManager
                        userName = "User Test",
                        rating = rating,
                        comment = reviewText,
                        date = System.currentTimeMillis()
                )

        reviewManager.addReview(review) { success ->
            if (success) {
                Toast.makeText(this, "Review berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Gagal menambahkan review", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
