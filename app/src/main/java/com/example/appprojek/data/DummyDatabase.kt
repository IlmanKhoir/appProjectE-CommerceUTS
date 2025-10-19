package com.example.appprojek.data

import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

/**
 * Very small in-memory dummy database for local development / UI-only mode.
 * Not persisted across process restarts.
 */
object DummyDatabase {
        /*
            Catatan santai:
            - Ini in-memory DB buat development/demo.
            - Data nggak persist setelah app di-kill.
            - updateProfile dibuat toleran: kalau nggak nemu user, bakal bikin record baru supaya UI nggak crash.
            - Kalau mau reset user demo, restart app atau ubah kode init() seed user di bawah.
            - Debug tip: buka logcat dan cari tag "DummyDatabase" buat pesan fallback atau info.
        */
    private data class UserRecord(
        val id: Int,
        var email: String,
        var name: String,
        var phone: String,
        var address: String,
        var password: String
    )

    private var nextUserId = 1
    private val usersByEmail = mutableMapOf<String, UserRecord>()

    private data class OrderRowInternal(
        val id: Int,
        val userId: Int,
        val totalAmount: Int,
        val status: String,
        val orderDateIso: String,
        val shippingAddress: String?,
        val paymentMethod: String?,
        val trackingNumber: String?,
        val items: List<OrderRepository.OrderItem>
    )

    private var nextOrderId = 1
    private val orders = mutableListOf<OrderRowInternal>()

    private val wishlists = mutableMapOf<Int, MutableSet<String>>() // userId -> set(productId)

    init {
        // seed a demo user
        val demo = UserRecord(nextUserId++, "user@example.com", "Pengguna Demo", "081234567890", "Jalan Demo No.1", "password")
        usersByEmail[demo.email] = demo
    }

    // Users
    fun register(email: String, password: String, name: String?, phone: String?, address: String?): Int? {
        if (usersByEmail.containsKey(email)) return null
        val id = nextUserId++
        val rec = UserRecord(id, email, name ?: "", phone ?: "", address ?: "", password)
        usersByEmail[email] = rec
        return id
    }

    fun login(email: String, password: String): Int? {
        val rec = usersByEmail[email] ?: return null
        return if (rec.password == password) rec.id else null
    }

    fun getProfileByEmail(email: String): Map<String, String?>? {
        val rec = usersByEmail[email] ?: return null
        return mapOf(
            "user_id" to rec.id.toString(),
            "email" to rec.email,
            "name" to rec.name,
            "phone" to rec.phone,
            "address" to rec.address
        )
    }

    fun updateProfile(userId: Int?, email: String?, name: String?, phone: String?, address: String?, latitude: Double?, longitude: Double?): Boolean {
        if (userId == null) return false

        var rec = usersByEmail.values.firstOrNull { it.id == userId }

        // If not found by id, try to find by email as a fallback (robustness for session/email drift)
        if (rec == null && email != null) {
            rec = usersByEmail[email]
            Log.w("DummyDatabase", "updateProfile: no record for id=$userId, fallback lookup by email=${email} -> ${rec != null}")
        }

        // If still not found, create a new record so UI/profile updates don't fail
        if (rec == null) {
            Log.w("DummyDatabase", "updateProfile: failed to find user record for userId=$userId and email=$email — creating new record")
            val newId = if (userId > 0) {
                // ensure nextUserId stays ahead
                if (userId >= nextUserId) {
                    nextUserId = userId + 1
                }
                userId
            } else {
                nextUserId++
            }
            val newEmail = email ?: "user${newId}@local"
            val newRec = UserRecord(newId, newEmail, name ?: "", phone ?: "", address ?: "", password = "")
            usersByEmail[newEmail] = newRec
            rec = newRec
            Log.i("DummyDatabase", "updateProfile: created fallback user record id=${rec.id} email=${rec.email}")
        }

        // If email is changing, we need to update the map key as well.
        if (email != null && email != rec.email) {
            // Remove old entry and re-insert under new email
            usersByEmail.remove(rec.email)
            rec.email = email
            usersByEmail[rec.email] = rec
        }
        name?.let { rec.name = it }
        phone?.let { rec.phone = it }
        address?.let { rec.address = it }

        Log.i("DummyDatabase", "updateProfile: userId=$userId, email=${rec.email}, name=${rec.name}, phone=${rec.phone}, address=${rec.address}, lat=$latitude, lng=$longitude")

        return true
    }

    // Orders
    private val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }

    fun listOrders(userId: Int): List<OrderRepository.OrderRow> {
        return orders.filter { it.userId == userId }.map {
            OrderRepository.OrderRow(
                id = it.id,
                total_amount = it.totalAmount,
                status = it.status,
                order_date = it.orderDateIso,
                shipping_address = it.shippingAddress,
                payment_method = it.paymentMethod,
                tracking_number = it.trackingNumber,
                items = it.items
            )
        }
    }

    fun createOrder(userId: Int, totalAmount: Int, status: String, shippingAddress: String, paymentMethod: String, trackingNumber: String?, items: List<OrderRepository.OrderItem>): Int {
        val id = nextOrderId++
        val row = OrderRowInternal(
            id = id,
            userId = userId,
            totalAmount = totalAmount,
            status = status,
            orderDateIso = isoFmt.format(Date()),
            shippingAddress = shippingAddress,
            paymentMethod = paymentMethod,
            trackingNumber = trackingNumber,
            items = items
        )
        orders.add(row)
        return id
    }

    // Wishlist
    fun listWishlist(userId: Int): List<WishlistRepository.WishlistItem> {
        val set = wishlists.getOrPut(userId) { mutableSetOf() }
        return set.map { WishlistRepository.WishlistItem(it, Date().toString()) }
    }

    fun addToWishlist(userId: Int, productId: String): Boolean {
        val set = wishlists.getOrPut(userId) { mutableSetOf() }
        return set.add(productId)
    }

    fun removeFromWishlist(userId: Int, productId: String): Boolean {
        val set = wishlists.getOrPut(userId) { mutableSetOf() }
        return set.remove(productId)
    }
}
