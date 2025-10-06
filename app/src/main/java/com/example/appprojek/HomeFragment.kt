package com.example.appprojek

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.appprojek.category.CategoryActivity
import com.example.appprojek.model.Category
import com.example.appprojek.model.Product
import com.example.appprojek.product.ProductDetailActivity
import com.example.appprojek.ui.BannerAdapter
import com.example.appprojek.ui.BannerItem
import com.example.appprojek.ui.CategoryAdapter
import com.example.appprojek.ui.ProductAdapter
import com.example.appprojek.util.AuthManager

class HomeFragment : Fragment() {
        private val authManager by lazy { AuthManager(requireContext()) }

        override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
        ): View? {
                return inflater.inflate(R.layout.fragment_home, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                super.onViewCreated(view, savedInstanceState)
                try {
                        setupProducts(view)
                        setupBanners(view)
                        setupCategories(view)
                } catch (e: Exception) {
                        Toast.makeText(
                                        requireContext(),
                                        "Terjadi kesalahan: ${e.message}",
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }
        }

        private fun setupProducts(view: View) {
                val recycler = view.findViewById<RecyclerView>(R.id.recyclerGrid)
                recycler.layoutManager = GridLayoutManager(requireContext(), 2)
                val products =
                        listOf(
                                Product(
                                        "p1",
                                        "Susu Kotak 1L",
                                        23000,
                                        imageResId = R.drawable.susu_kotak,
                                        description = "Susu segar dalam kemasan kotak"
                                ),
                                Product(
                                        "p2",
                                        "Roti Tawar",
                                        18000,
                                        imageResId = R.drawable.roti_tawar, 
                                        description = "Roti tawar lembut dan segar"
                                ),
                                Product(
                                        "p3",
                                        "Mie Instan Goreng",
                                        3500,
                                        imageResId = R.drawable.mie_instant, 
                                        description = "Mie instan rasa goreng"
                                ),
                                Product(
                                        "p4",
                                        "Minyak Goreng 1L",
                                        15500,
                                        imageResId =
                                                R.drawable.minyak_goreng, 
                                        description = "Minyak goreng berkualitas tinggi"
                                ),
                                Product(
                                        "p5",
                                        "Beras Premium 5kg",
                                        79000,
                                        imageResId =
                                                R.drawable.beras_premium, 
                                        description = "Beras premium kualitas terbaik"
                                ),
                                Product(
                                        "p6",
                                        "Snack Kentang",
                                        12000,
                                        imageResId =
                                                R.drawable.snack_kentang,
                                        description = "Snack kentang renyah"
                                ),
                                Product(
                                        "p7",
                                        "Teh Botol",
                                        5000,
                                        imageResId = R.drawable.teh_botol, 
                                        description = "Teh botol segar"
                                ),
                                Product(
                                        "p8",
                                        "Kopi Susu",
                                        8000,
                                        imageResId = R.drawable.kopi_susu, 
                                        description = "Kopi susu nikmat"
                                )
                        )
                recycler.adapter =
                        ProductAdapter(
                                products,
                                onAddedToCart = { product ->
                                        Toast.makeText(
                                                        requireContext(),
                                                        "${product.name} ditambahkan ke keranjang",
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                },
                                onProductClick = { product ->
                                        try {
                                                val intent =
                                                        Intent(
                                                                requireContext(),
                                                                ProductDetailActivity::class.java
                                                        )
                                                intent.putExtra("product", product)
                                                startActivity(intent)
                                        } catch (e: Exception) {
                                                Toast.makeText(
                                                                requireContext(),
                                                                "Terjadi kesalahan: ${e.message}",
                                                                Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                        }
                                }
                        )
        }

        private fun setupBanners(view: View) {
                val bannerRv = view.findViewById<RecyclerView>(R.id.recyclerBanner)
                bannerRv.layoutManager =
                        androidx.recyclerview.widget.LinearLayoutManager(
                                requireContext(),
                                RecyclerView.HORIZONTAL,
                                false
                        )
                val banners =
                        listOf(
                                BannerItem("Promo Spesial", R.mipmap.ic_launcher),
                                BannerItem("Gratis Ongkir", R.mipmap.ic_launcher),
                                BannerItem("Flash Sale", R.mipmap.ic_launcher)
                        )
                bannerRv.adapter = BannerAdapter(banners)
                PagerSnapHelper().attachToRecyclerView(bannerRv)

                // Auto-scroll banner
                bannerRv.post(
                        object : Runnable {
                                var i = 0
                                override fun run() {
                                        try {
                                                if (banners.isNotEmpty()) {
                                                        i = (i + 1) % banners.size
                                                        bannerRv.smoothScrollToPosition(i)
                                                }
                                                bannerRv.postDelayed(this, 2500)
                                        } catch (e: Exception) {
                                                // Stop auto-scroll if error occurs
                                        }
                                }
                        }
                )
        }

        private fun setupCategories(view: View) {
                val categoryRv = view.findViewById<RecyclerView>(R.id.recyclerCategories)
                categoryRv.layoutManager =
                        androidx.recyclerview.widget.LinearLayoutManager(
                                requireContext(),
                                RecyclerView.HORIZONTAL,
                                false
                        )
                val categories =
                        listOf(
                                Category("food", "Makanan & Minuman", R.drawable.ic_food),
                                Category("kitchen", "Dapur & Masak", R.drawable.ic_kitchen),
                                Category("snack", "Snack & Camilan", R.drawable.ic_snack),
                                Category("drink", "Minuman", R.drawable.ic_drink)
                        )
                categoryRv.adapter =
                        CategoryAdapter(categories) { category ->
                                try {
                                        val intent =
                                                Intent(
                                                        requireContext(),
                                                        CategoryActivity::class.java
                                                )
                                        intent.putExtra("category_id", category.id)
                                        intent.putExtra("category_name", category.name)
                                        startActivity(intent)
                                } catch (e: Exception) {
                                        Toast.makeText(
                                                        requireContext(),
                                                        "Terjadi kesalahan: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                }
                        }
        }
}
