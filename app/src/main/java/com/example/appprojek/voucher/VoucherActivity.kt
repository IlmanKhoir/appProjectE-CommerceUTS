package com.example.appprojek.voucher

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appprojek.databinding.ActivityVoucherBinding
import com.example.appprojek.model.DiscountType
import com.example.appprojek.model.Voucher
import com.example.appprojek.ui.VoucherAdapter

class VoucherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVoucherBinding
    private lateinit var voucherAdapter: VoucherAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoucherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadVouchers()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.recyclerVouchers.layoutManager = LinearLayoutManager(this)
        voucherAdapter =
                VoucherAdapter(
                        emptyList(),
                        onVoucherClick = { voucher -> handleVoucherClick(voucher) }
                )
        binding.recyclerVouchers.adapter = voucherAdapter

        binding.btnApplyVoucher.setOnClickListener { applyVoucher() }
    }

    private fun loadVouchers() {
        // Simulasi data voucher
        val vouchers =
                listOf(
                        Voucher(
                                id = "v1",
                                code = "WELCOME20",
                                name = "Welcome Discount",
                                description = "Dapatkan diskon 20% untuk pembelian pertama",
                                discountType = DiscountType.PERCENTAGE,
                                discountValue = 20,
                                minOrderAmount = 50000,
                                maxDiscountAmount = 25000,
                                validFrom = System.currentTimeMillis() - 86400000,
                                validUntil = System.currentTimeMillis() + 86400000 * 30,
                                usageLimit = 1000,
                                usedCount = 250
                        ),
                        Voucher(
                                id = "v2",
                                code = "FREESHIP",
                                name = "Gratis Ongkir",
                                description = "Gratis ongkir untuk pembelian minimal Rp 100.000",
                                discountType = DiscountType.FREE_SHIPPING,
                                discountValue = 0,
                                minOrderAmount = 100000,
                                validFrom = System.currentTimeMillis() - 86400000,
                                validUntil = System.currentTimeMillis() + 86400000 * 7,
                                usageLimit = 500,
                                usedCount = 150
                        ),
                        Voucher(
                                id = "v3",
                                code = "FLASH50",
                                name = "Flash Sale 50%",
                                description = "Diskon 50% untuk produk pilihan",
                                discountType = DiscountType.PERCENTAGE,
                                discountValue = 50,
                                minOrderAmount = 75000,
                                maxDiscountAmount = 50000,
                                validFrom = System.currentTimeMillis() - 3600000,
                                validUntil = System.currentTimeMillis() + 3600000 * 24,
                                usageLimit = 200,
                                usedCount = 180
                        ),
                        Voucher(
                                id = "v4",
                                code = "CASHBACK10K",
                                name = "Cashback Rp 10.000",
                                description =
                                        "Dapatkan cashback Rp 10.000 untuk pembelian minimal Rp 150.000",
                                discountType = DiscountType.FIXED_AMOUNT,
                                discountValue = 10000,
                                minOrderAmount = 150000,
                                validFrom = System.currentTimeMillis() - 86400000,
                                validUntil = System.currentTimeMillis() + 86400000 * 14,
                                usageLimit = 1000,
                                usedCount = 300
                        )
                )
        voucherAdapter.updateVouchers(vouchers)
    }

    private fun handleVoucherClick(voucher: Voucher) {
        binding.etVoucherCode.setText(voucher.code)
    }

    private fun applyVoucher() {
        val voucherCode = binding.etVoucherCode.text.toString().trim()
        if (voucherCode.isEmpty()) {
            Toast.makeText(this, "Masukkan kode voucher", Toast.LENGTH_SHORT).show()
            return
        }

        // Simulasi validasi voucher
        val isValid =
                voucherCode.uppercase() in listOf("WELCOME20", "FREESHIP", "FLASH50", "CASHBACK10K")
        if (isValid) {
            Toast.makeText(this, "Voucher berhasil diterapkan!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Kode voucher tidak valid", Toast.LENGTH_SHORT).show()
        }
    }
}
