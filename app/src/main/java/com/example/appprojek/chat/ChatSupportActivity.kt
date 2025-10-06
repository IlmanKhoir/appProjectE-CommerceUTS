package com.example.appprojek.chat

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appprojek.databinding.ActivityChatSupportBinding
import com.example.appprojek.model.ChatMessage
import com.example.appprojek.model.MessageType
import com.example.appprojek.ui.ChatAdapter

class ChatSupportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatSupportBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadInitialMessages()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.recyclerMessages.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(messages)
        binding.recyclerMessages.adapter = chatAdapter

        binding.btnSend.setOnClickListener { sendMessage() }

        // Auto-scroll to bottom when new message is added
        chatAdapter.registerAdapterDataObserver(
                object : androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        super.onItemRangeInserted(positionStart, itemCount)
                        binding.recyclerMessages.scrollToPosition(messages.size - 1)
                    }
                }
        )
    }

    private fun loadInitialMessages() {
        // Simulasi pesan awal dari customer support
        val initialMessages =
                listOf(
                        ChatMessage(
                                id = "msg1",
                                message =
                                        "Halo! Selamat datang di layanan customer support. Ada yang bisa saya bantu?",
                                isFromUser = false,
                                senderName = "Customer Support",
                                messageType = MessageType.SYSTEM
                        )
                )
        messages.addAll(initialMessages)
        chatAdapter.notifyDataSetChanged()
    }

    private fun sendMessage() {
        val messageText = binding.etMessage.text.toString().trim()
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Masukkan pesan", Toast.LENGTH_SHORT).show()
            return
        }

        // Add user message
        val userMessage =
                ChatMessage(
                        id = "user_${System.currentTimeMillis()}",
                        message = messageText,
                        isFromUser = true,
                        senderName = "Anda"
                )
        messages.add(userMessage)
        chatAdapter.notifyItemInserted(messages.size - 1)

        // Clear input
        binding.etMessage.text?.clear()

        // Simulate bot response
        simulateBotResponse(messageText)
    }

    private fun simulateBotResponse(userMessage: String) {
        // Simulasi response otomatis berdasarkan kata kunci
        val response =
                when {
                    userMessage.contains("pesanan", ignoreCase = true) -> {
                        "Untuk informasi pesanan, Anda dapat mengecek di menu 'Riwayat Pesanan' di profil Anda. Apakah ada nomor pesanan spesifik yang ingin Anda tanyakan?"
                    }
                    userMessage.contains("pengiriman", ignoreCase = true) ||
                            userMessage.contains("ongkir", ignoreCase = true) -> {
                        "Kami menyediakan layanan pengiriman ke seluruh Indonesia. Ongkir akan dihitung berdasarkan lokasi tujuan dan berat barang. Apakah ada alamat spesifik yang ingin Anda tanyakan?"
                    }
                    userMessage.contains("pembayaran", ignoreCase = true) ||
                            userMessage.contains("bayar", ignoreCase = true) -> {
                        "Kami menerima berbagai metode pembayaran: Transfer Bank, E-Wallet (GoPay, OVO, DANA), dan COD untuk area tertentu. Apakah ada metode pembayaran yang ingin Anda ketahui lebih lanjut?"
                    }
                    userMessage.contains("voucher", ignoreCase = true) ||
                            userMessage.contains("diskon", ignoreCase = true) -> {
                        "Kami memiliki berbagai voucher dan promo menarik! Anda dapat melihat voucher yang tersedia di menu 'Voucher & Promo' atau saat checkout. Ada kode voucher spesifik yang ingin Anda gunakan?"
                    }
                    userMessage.contains("retur", ignoreCase = true) ||
                            userMessage.contains("refund", ignoreCase = true) -> {
                        "Kami menyediakan layanan retur dan refund sesuai dengan kebijakan yang berlaku. Produk dapat dikembalikan dalam kondisi baik dalam 7 hari setelah diterima. Apakah ada produk yang ingin Anda retur?"
                    }
                    else -> {
                        "Terima kasih atas pesan Anda. Tim customer support kami akan segera merespons. Sementara itu, apakah ada hal lain yang bisa saya bantu?"
                    }
                }

        // Simulate delay for bot response
        binding.etMessage.postDelayed(
                {
                    val botMessage =
                            ChatMessage(
                                    id = "bot_${System.currentTimeMillis()}",
                                    message = response,
                                    isFromUser = false,
                                    senderName = "Customer Support"
                            )
                    messages.add(botMessage)
                    chatAdapter.notifyItemInserted(messages.size - 1)
                },
                1500
        )
    }
}
