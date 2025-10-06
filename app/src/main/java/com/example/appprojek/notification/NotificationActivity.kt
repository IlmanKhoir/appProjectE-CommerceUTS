package com.example.appprojek.notification

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appprojek.databinding.ActivityNotificationBinding
import com.example.appprojek.model.Notification
import com.example.appprojek.model.NotificationType
import com.example.appprojek.ui.NotificationAdapter

class NotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadNotifications()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.recyclerNotifications.layoutManager = LinearLayoutManager(this)
        notificationAdapter =
                NotificationAdapter(
                        emptyList(),
                        onNotificationClick = { notification ->
                            handleNotificationClick(notification)
                        }
                )
        binding.recyclerNotifications.adapter = notificationAdapter

        binding.btnMarkAllRead.setOnClickListener { markAllAsRead() }
    }

    private fun loadNotifications() {
        // Simulasi data notifikasi
        val notifications =
                listOf(
                        Notification(
                                id = "notif1",
                                title = "Promo Spesial!",
                                message =
                                        "Dapatkan diskon 20% untuk semua produk makanan. Berlaku hingga 31 Januari 2024.",
                                type = NotificationType.PROMO,
                                timestamp = System.currentTimeMillis() - 3600000, // 1 jam lalu
                                actionUrl = "promo://special"
                        ),
                        Notification(
                                id = "notif2",
                                title = "Pesanan Sedang Dikirim",
                                message =
                                        "Pesanan #ORD002 sedang dalam perjalanan. Nomor tracking: TRK987654321",
                                type = NotificationType.ORDER_UPDATE,
                                timestamp = System.currentTimeMillis() - 7200000, // 2 jam lalu
                                actionUrl = "order://track/ORD002"
                        ),
                        Notification(
                                id = "notif3",
                                title = "Pesanan Selesai",
                                message =
                                        "Pesanan #ORD001 telah berhasil diterima. Terima kasih telah berbelanja!",
                                type = NotificationType.ORDER_UPDATE,
                                timestamp = System.currentTimeMillis() - 86400000, // 1 hari lalu
                                actionUrl = "order://detail/ORD001"
                        ),
                        Notification(
                                id = "notif4",
                                title = "Flash Sale Weekend",
                                message =
                                        "Jangan lewatkan flash sale weekend! Diskon hingga 50% untuk produk pilihan.",
                                type = NotificationType.PROMO,
                                timestamp = System.currentTimeMillis() - 172800000, // 2 hari lalu
                                actionUrl = "promo://flashsale"
                        ),
                        Notification(
                                id = "notif5",
                                title = "Update Aplikasi",
                                message =
                                        "Aplikasi telah diperbarui dengan fitur-fitur baru. Update sekarang untuk pengalaman terbaik!",
                                type = NotificationType.SYSTEM,
                                timestamp = System.currentTimeMillis() - 259200000, // 3 hari lalu
                                actionUrl = "system://update"
                        )
                )
        notificationAdapter.updateNotifications(notifications)
    }

    private fun handleNotificationClick(notification: Notification) {
        when (notification.type) {
            NotificationType.PROMO -> {
                Toast.makeText(this, "Membuka halaman promo", Toast.LENGTH_SHORT).show()
            }
            NotificationType.ORDER_UPDATE -> {
                Toast.makeText(this, "Membuka detail pesanan", Toast.LENGTH_SHORT).show()
            }
            NotificationType.GENERAL -> {
                Toast.makeText(this, "Membuka notifikasi umum", Toast.LENGTH_SHORT).show()
            }
            NotificationType.SYSTEM -> {
                Toast.makeText(this, "Membuka pengaturan sistem", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun markAllAsRead() {
        notificationAdapter.markAllAsRead()
        Toast.makeText(this, "Semua notifikasi ditandai sebagai sudah dibaca", Toast.LENGTH_SHORT)
                .show()
    }
}
