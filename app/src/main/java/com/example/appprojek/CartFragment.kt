package com.example.appprojek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appprojek.cart.CartManager
import com.example.appprojek.mock.MockDataProvider
import com.example.appprojek.ui.CartAdapter
import com.example.appprojek.util.GeoUtils

class CartFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    // Note: removed automatic seeding of cart to avoid overwriting user's selections
    // MockDataProvider.seedCart()
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerCart)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        lateinit var adapter: com.example.appprojek.ui.CartAdapter
        adapter = com.example.appprojek.ui.CartAdapter(CartManager.getItems().toMutableList()) { productId ->
            // remove one quantity of this product and refresh
            CartManager.removeOne(productId)
            adapter.updateItems(CartManager.getItems())
            val totalText = view.findViewById<TextView>(R.id.textTotal)
            totalText.text = "Total: Rp %,d".format(CartManager.getTotalRupiah()).replace(',', '.')
        }
        recycler.adapter = adapter
        val totalText = view.findViewById<TextView>(R.id.textTotal)
        totalText.text = "Total: Rp %,d".format(CartManager.getTotalRupiah()).replace(',', '.')

        val distanceEtaText = view.findViewById<TextView>(R.id.textDistanceEta)
        // Contoh: lokasi toko dan rumah (dummy). Nanti bisa dihubungkan dengan GPS pengguna
        val storeLat = -6.200000
        val storeLng = 106.816666
        val homeLat = -6.230000
        val homeLng = 106.820000
        val dKm = GeoUtils.distanceKm(storeLat, storeLng, homeLat, homeLng)
        val etaMin = GeoUtils.estimateEtaMinutes(dKm)
        distanceEtaText.text = "Jarak: %.1f km  •  ETA: %d menit".format(dKm, etaMin)

        view.findViewById<android.view.View>(R.id.buttonPay).setOnClickListener {
            startActivity(
                    android.content.Intent(
                            requireContext(),
                            com.example.appprojek.payment.PaymentActivity::class.java
                    )
            )
        }
    }
}
