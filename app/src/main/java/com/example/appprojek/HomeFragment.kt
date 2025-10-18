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
import com.example.appprojek.mock.MockDataProvider
import com.example.appprojek.product.ProductDetailActivity
import com.example.appprojek.ui.BannerAdapter
import com.example.appprojek.ui.BannerItem
import com.example.appprojek.ui.CategoryAdapter
import com.example.appprojek.ui.ProductAdapter
import com.example.appprojek.util.AuthManager


class HomeFragment : Fragment() {
        private lateinit var authManager: AuthManager
        private lateinit var adapter: ProductAdapter
        private lateinit var products: List<Product>

        override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
        ): View? {
                return inflater.inflate(R.layout.fragment_home, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                super.onViewCreated(view, savedInstanceState)
                authManager = AuthManager(view.context)
                try {
                        setupBanners(view)
                        setupCategories(view)
                        products = MockDataProvider.getProducts()
                        val recycler = view.findViewById<RecyclerView>(R.id.recyclerGrid)
                        recycler.layoutManager = GridLayoutManager(view.context, 2)
                        adapter = ProductAdapter(
                                products,
                                onAddedToCart = { product ->
                                        Toast.makeText(
                                                view.context,
                                                "${product.name} ditambahkan ke keranjang",
                                                Toast.LENGTH_SHORT
                                        ).show()
                                },
                                onProductClick = { product ->
                                        try {
                                                val intent = Intent(view.context, ProductDetailActivity::class.java)
                                                intent.putExtra("product", product)
                                                requireActivity().startActivity(intent)
                                        } catch (e: Exception) {
                                                Toast.makeText(view.context, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                        )
                        recycler.adapter = adapter
                                        val etSearch = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSearchHome)
                                        val searchLayout = view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.searchLayout)
                                        searchLayout.setEndIconOnClickListener {
                                            val query = etSearch.text?.toString() ?: ""
                                            val filtered = products.filter {
                                                it.name.contains(query, ignoreCase = true) ||
                                                        it.description.contains(query, ignoreCase = true)
                                            }
                                            adapter.updateProducts(filtered)
                                        }
                } catch (e: Exception) {
                        Toast.makeText(
                                view.context,
                                "Terjadi kesalahan: ${e.message}",
                                Toast.LENGTH_SHORT
                        ).show()
                }
        }

        // products are provided by MockDataProvider; removed inline list
        }

    // setupProducts dihapus, logika grid sudah di onViewCreated

        private fun setupBanners(view: View) {
                val bannerRv = view.findViewById<RecyclerView>(R.id.recyclerBanner)
                bannerRv.layoutManager =
                        androidx.recyclerview.widget.LinearLayoutManager(
                                view.context,
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
                                view.context,
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
                    val intent = Intent(view.context, CategoryActivity::class.java)
                    intent.putExtra("category_id", category.id)
                    intent.putExtra("category_name", category.name)
                    view.context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        view.context,
                        "Terjadi kesalahan: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
