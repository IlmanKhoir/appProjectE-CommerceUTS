package com.example.appprojek.mock

import com.example.appprojek.R
import com.example.appprojek.cart.CartManager
import com.example.appprojek.model.*

object MockDataProvider {
    fun getProducts(): List<Product> = listOf(
        Product("p1", "Susu Kotak 1L", 23000, imageResId = R.drawable.susu_kotak, description = "Susu segar dalam kemasan kotak"),
        Product("p2", "Roti Tawar", 18000, imageResId = R.drawable.roti_tawar, description = "Roti tawar lembut dan segar"),
        Product("p3", "Mie Instan Goreng", 3500, imageResId = R.drawable.mie_instant, description = "Mie instan rasa goreng"),
        Product("p4", "Minyak Goreng 1L", 15500, imageResId = R.drawable.minyak_goreng, description = "Minyak goreng berkualitas tinggi"),
        Product("p5", "Beras Premium 5kg", 79000, imageResId = R.drawable.beras_premium, description = "Beras premium kualitas terbaik"),
        Product("p6", "Snack Kentang", 12000, imageResId = R.drawable.snack_kentang, description = "Snack kentang renyah"),
        Product("p7", "Teh Botol", 5000, imageResId = R.drawable.teh_botol, description = "Teh botol segar"),
        Product("p8", "Kopi Susu", 8000, imageResId = R.drawable.kopi_susu, description = "Kopi susu nikmat")
    )

    fun seedCart() {
        CartManager.clear()
        val products = getProducts()
        // add first product as quantity 1 to cart
        if (products.isNotEmpty()) {
            // CartManager provides add(product) which increments quantity by 1
            CartManager.add(products[0])
        }
    }

    fun getUser(): User = User(
        id = "u1",
        email = "user@example.com",
        name = "Pengguna Demo",
        phone = "081234567890",
        address = "Jalan Demo No.1"
    )

    fun getNotifications(): List<Notification> = listOf(
        Notification("n1", "Promo Spesial", "Diskon 20% untuk semua produk hari ini", NotificationType.PROMO),
        Notification("n2", "Order Dikirim", "Pesanan #ORD-001 telah dikirim", NotificationType.ORDER_UPDATE)
    )

    fun getVouchers(): List<Voucher> {
        val now = System.currentTimeMillis()
        return listOf(
            Voucher("v1", "PROMO20", "Diskon 20%", "Diskon 20% untuk pembelian di atas Rp50.000", DiscountType.PERCENTAGE, 20, minOrderAmount = 50000, validFrom = now - 86400000, validUntil = now + 7 * 86400000),
            Voucher("v2", "ONGKIRFREE", "Gratis Ongkir", "Gratis ongkir untuk semua kecamatan", DiscountType.FREE_SHIPPING, 0, validFrom = now - 86400000, validUntil = now + 30 * 86400000)
        )
    }
}
