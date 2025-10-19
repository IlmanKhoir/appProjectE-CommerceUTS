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
import com.google.android.material.chip.Chip
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var productAdapter: ProductAdapter
    private val allProducts = mutableListOf<Product>()
    private val spanCount = 2
    private val PREFS = "search_prefs"
    private val KEY_RECENT = "recent_list"
    private val MAX_RECENT = 6
    private val handler = Handler(Looper.getMainLooper())
    private var debounceRunnable: Runnable? = null
    private val DEBOUNCE_MS = 350L

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

    // Real-time search with TextWatcher
    binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // debounce to avoid filtering on every keystroke
            debounceRunnable?.let { handler.removeCallbacks(it) }
            val q = s?.toString() ?: ""
            binding.progressSearch.visibility = android.view.View.VISIBLE
            debounceRunnable = Runnable {
                binding.progressSearch.visibility = android.view.View.GONE
                // Show filtered recent suggestions
                filterRecentSuggestions(q)
                filterProducts(q)
            }
            handler.postDelayed(debounceRunnable!!, DEBOUNCE_MS)
        }
        override fun afterTextChanged(s: android.text.Editable?) {}
    })

    // Handle IME search action (submit)
    binding.etSearch.setOnEditorActionListener { v, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            val q = v.text?.toString() ?: ""
            // hide keyboard
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
            // immediate search and save
            debounceRunnable?.let { handler.removeCallbacks(it) }
            binding.progressSearch.visibility = android.view.View.VISIBLE
            handler.postDelayed({
                binding.progressSearch.visibility = android.view.View.GONE
                filterProducts(q)
                if (q.isNotBlank()) saveRecentQuery(q)
            }, 100)
            true
        } else false
    }

    binding.recyclerProducts.layoutManager = GridLayoutManager(this, spanCount)
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
    // spacing decoration
    val spacingPx = (8 * resources.displayMetrics.density).toInt()
    binding.recyclerProducts.addItemDecoration(com.example.appprojek.ui.GridSpacingItemDecoration(spanCount, spacingPx, true))

    // Note: clear button handled by TextInputLayout end icon in layout if needed

    // Recent searches setup
    loadRecentSearches()
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
            productAdapter.setHighlightQuery(null)
            binding.tvNoResults.visibility = android.view.View.GONE
        } else {
            val filteredProducts =
                    allProducts.filter { product ->
                        product.name.contains(query, ignoreCase = true) ||
                                product.description.contains(query, ignoreCase = true)
                    }
            productAdapter.updateProducts(filteredProducts)
            productAdapter.setHighlightQuery(query)
            binding.tvNoResults.visibility = if (filteredProducts.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            // Save to recent if not empty and not duplicate
            if (query.isNotBlank()) saveRecentQuery(query)
        }
    }

    private fun saveRecentQuery(query: String) {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val current = prefs.getString(KEY_RECENT, "") ?: ""
        val items = if (current.isBlank()) mutableListOf<String>() else current.split("||").toMutableList()
        // remove existing duplicate
        items.removeAll { it.equals(query, ignoreCase = true) }
        items.add(0, query)
        while (items.size > MAX_RECENT) items.removeLast()
        prefs.edit().putString(KEY_RECENT, items.joinToString("||")).apply()
        renderRecent(items)
    }

    private fun loadRecentSearches() {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val current = prefs.getString(KEY_RECENT, "") ?: ""
        val items = if (current.isBlank()) listOf() else current.split("||").filter { it.isNotBlank() }
        renderRecent(items.toMutableList())
    }

    private fun renderRecent(items: MutableList<String>) {
        val group = binding.chipGroupRecent
        group.removeAllViews()
        if (items.isEmpty()) {
            binding.tvRecentLabel.visibility = android.view.View.GONE
            binding.chipGroupRecent.visibility = android.view.View.GONE
            binding.btnClearHistory.visibility = android.view.View.GONE
            return
        }
        binding.tvRecentLabel.visibility = android.view.View.VISIBLE
        binding.chipGroupRecent.visibility = android.view.View.VISIBLE
        binding.btnClearHistory.visibility = android.view.View.VISIBLE
        for (q in items) {
            val chip = Chip(this)
            chip.text = q
            chip.isClickable = true
            chip.setOnClickListener {
                binding.etSearch.setText(q)
                filterProducts(q)
            }
            group.addView(chip)
        }
        binding.btnClearHistory.setOnClickListener {
            getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_RECENT).apply()
            renderRecent(mutableListOf())
        }
    }

    private fun filterRecentSuggestions(query: String) {
        if (query.isBlank()) {
            loadRecentSearches()
            return
        }
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val current = prefs.getString(KEY_RECENT, "") ?: ""
        val items = if (current.isBlank()) listOf() else current.split("||").filter { it.isNotBlank() }
        val filtered = items.filter { it.contains(query, ignoreCase = true) }
        renderRecent(filtered.toMutableList())
    }
}
