package com.example.appprojek.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appprojek.databinding.ActivityNotificationBinding
import com.example.appprojek.model.Notification
import com.example.appprojek.model.NotificationType
import com.example.appprojek.ui.NotificationAdapter

class NotificationFragment : Fragment() {
    private var _binding: ActivityNotificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityNotificationBinding.inflate(inflater, container, false)
        setupUI()
        loadNotifications()
        return binding.root
    }

    private fun setupUI() {
        binding.toolbar.visibility = View.GONE // Hide toolbar in fragment
        binding.recyclerNotifications.layoutManager = LinearLayoutManager(requireContext())
        notificationAdapter = NotificationAdapter(
            emptyList(),
            onNotificationClick = { notification -> handleNotificationClick(notification) }
        )
        binding.recyclerNotifications.adapter = notificationAdapter
        binding.btnMarkAllRead.setOnClickListener { markAllAsRead() }
    }

    private fun loadNotifications() {
        val notifications = listOf(
            Notification(
                id = "notif1",
                title = "Promo Spesial!",
                message = "Dapatkan diskon 20% untuk semua produk makanan. Berlaku hingga 31 Januari 2024.",
                type = NotificationType.PROMO,
                timestamp = System.currentTimeMillis() - 3600000,
                actionUrl = "promo://special"
            ),
            Notification(
                id = "notif2",
                title = "Pesanan Sedang Dikirim",
                message = "Pesanan #ORD002 sedang dalam perjalanan. Nomor tracking: TRK987654321",
                type = NotificationType.ORDER_UPDATE,
                timestamp = System.currentTimeMillis() - 7200000,
                actionUrl = "order://ORD002"
            )
        )
        notificationAdapter.updateNotifications(notifications)
    }

    private fun handleNotificationClick(notification: Notification) {
        Toast.makeText(requireContext(), "Klik: ${notification.title}", Toast.LENGTH_SHORT).show()
    }

    private fun markAllAsRead() {
        Toast.makeText(requireContext(), "Semua notifikasi ditandai dibaca", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
