package com.example.appprojek.category

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.appprojek.R
import com.example.appprojek.databinding.ActivityCategoryBinding
import com.example.appprojek.model.Product
import com.example.appprojek.product.ProductDetailActivity
import com.example.appprojek.ui.ProductAdapter

class CategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryBinding
    private lateinit var productAdapter: ProductAdapter
    private val allProducts = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryId = intent.getStringExtra("category_id") ?: ""
        val categoryName = intent.getStringExtra("category_name") ?: "Kategori"

        setupUI(categoryName)
        loadProducts()
        filterProductsByCategory(categoryId)
    }

    private fun setupUI(categoryName: String) {
        binding.toolbar.title = categoryName
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
                            val intent = Intent(this, ProductDetailActivity::class.java)
                            intent.putExtra("product", product)
                            startActivity(intent)
                        }
                )
        binding.recyclerProducts.adapter = productAdapter
    }

    private fun loadProducts() {
        // Simulasi data produk berdasarkan kategori
        allProducts.addAll(
                listOf(
                        // Makanan & Minuman
                        Product(

                                "p1",
                                "Susu Kotak 1L",
                                23000,
                                description = "Susu segar dalam kemasan kotak",
                                imageResId = R.drawable.susu_kotak,
                        ),
                        Product(
                                "p2",
                                "Roti Tawar",
                                18000,
                                description = "Roti tawar lembut dan segar",
                                imageResId = R.drawable.roti_tawar,
                        ),
                        Product(
                                "p3",
                                "Mie Instan Goreng",
                                3500,
                                description = "Mie instan rasa goreng",
                                imageResId = R.drawable.mie_instant,
                        ),
                        Product("p7", "Teh Botol", 5000, description = "Teh botol segar", imageResId = R.drawable.teh_botol),
                        Product("p8", "Kopi Susu", 8000, description = "Kopi susu nikmat", imageResId = R.drawable.kopi_susu),
                        Product(
                                "p9",
                                "Air Mineral 600ml",
                                3000,
                                description = "Air mineral kemasan botol",
                                imageResId = R.drawable.airmineral
                        ),
                        Product(
                                "p13",
                                "Yogurt Strawberry",
                                12000,
                                description = "Yogurt rasa strawberry",
                                imageResId = R.drawable.yogurtstrawberry
                        ),
                        Product("p15", "Jus Jeruk", 8000, description = "Jus jeruk segar"),

                        // Dapur & Masak
                        Product(
                                "p4",
                                "Minyak Goreng 1L",
                                15500,
                                description = "Minyak goreng berkualitas tinggi",
                                imageResId = R.drawable.minyak_goreng

                        ),
                        Product(
                                "p5",
                                "Beras Premium 5kg",
                                79000,
                                description = "Beras premium kualitas terbaik",
                                imageResId = R.drawable.beras_premium
                        ),
                        Product("p11", "Sosis Ayam", 25000, description = "Sosis ayam berkualitas", imageResId = R.drawable.sosisayam),
                        Product("p12", "Keju Cheddar", 35000, description = "Keju cheddar asli", imageResId = R.drawable.keju),

                        // Snack & Camilan
                        Product("p6", "Snack Kentang", 12000, description = "Snack kentang renyah", imageResId = R.drawable.snack_kentang),
                        Product(
                                "p10",
                                "Biskuit Coklat",
                                15000,
                                description = "Biskuit dengan coklat premium",
                                imageResId = R.drawable.biskuit_coklat
                        ),
                        Product(
                                "p14",
                                "Cereal Sarapan",
                                28000,
                                description = "Cereal untuk sarapan sehat",
                                imageResId = R.drawable.cereal
                        ),
                        Product("p16", "Kerupuk Udang", 18000, description = "Kerupuk udang renyah", imageResId = R.drawable.kerupuk_udang)
                )
        )
    }

    private fun filterProductsByCategory(categoryId: String) {
        val filteredProducts =
                when (categoryId) {
                    "food" ->
                            allProducts.filter {
                                it.id in listOf("p1", "p2", "p3", "p7", "p8", "p9", "p13", "p15")
                            }
                    "kitchen" -> allProducts.filter { it.id in listOf("p4", "p5", "p11", "p12") }
                    "snack" -> allProducts.filter { it.id in listOf("p6", "p10", "p14", "p16") }
                    else -> allProducts
                }
        productAdapter.updateProducts(filteredProducts)
    }
}
