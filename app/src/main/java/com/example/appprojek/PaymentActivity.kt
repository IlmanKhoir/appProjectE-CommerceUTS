package com.example.appprojek.payment

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.appprojek.R
import com.example.appprojek.domain.CartServiceAdapter
import com.example.appprojek.domain.PaymentMethod
import com.example.appprojek.presentation.PaymentPresenter
import com.example.appprojek.order.OrderSummaryActivity
import com.example.appprojek.util.AuthManager
import com.example.appprojek.util.CurrencyFormatter

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        val toolbar =
                findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val rbBank = findViewById<RadioButton>(R.id.rbBank)
        val rbEwallet = findViewById<RadioButton>(R.id.rbEwallet)
        val rbCod = findViewById<RadioButton>(R.id.rbCod)

        val textSubtotal = findViewById<TextView>(R.id.textSubtotal)
        val textShipping = findViewById<TextView>(R.id.textShipping)
        val textDiscount = findViewById<TextView>(R.id.textDiscount)
        val textTotalPay = findViewById<TextView>(R.id.textTotalPay)
        val editVoucher = findViewById<EditText>(R.id.editVoucher)

        val presenter = PaymentPresenter(CartServiceAdapter())
        fun bindSummary() {
            val s = presenter.computeSummary()
            textSubtotal.text = CurrencyFormatter.formatRupiah(s.subtotal)
            textShipping.text = CurrencyFormatter.formatRupiah(s.shipping)
            textDiscount.text = "- ${CurrencyFormatter.formatRupiah(s.discount)}"
            textTotalPay.text = CurrencyFormatter.formatRupiah(s.total)
        }
        bindSummary()

        findViewById<View>(R.id.buttonApplyVoucher).setOnClickListener {
            val code = editVoucher.text?.toString()?.trim() ?: ""
            presenter.applyVoucher(code)
            bindSummary()
        }

        var selectedMethodName: String? = null

        fun showBankOptions() {
            val items = arrayOf("Transfer BCA", "Transfer BRI", "Transfer BNI", "Transfer Mandiri")
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.payment_bank))
                    .setItems(items) { _, which -> selectedMethodName = items[which] }
                    .show()
        }

        fun showEwalletOptions() {
            val items = arrayOf("GoPay", "OVO", "DANA", "ShopeePay")
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.payment_ewallet))
                    .setItems(items) { _, which -> selectedMethodName = items[which] }
                    .show()
        }

        val payButton = findViewById<Button>(R.id.buttonConfirmPay)
        payButton.setOnClickListener {
            // Block jika keranjang kosong
            val cartIsEmpty = CartServiceAdapter().getItems().isEmpty()
            if (cartIsEmpty) {
                android.widget.Toast.makeText(this, "Keranjang masih kosong", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Determine selected method name
            when (presenter.selectedMethod) {
                PaymentMethod.BANK -> if (selectedMethodName == null) showBankOptions()
                PaymentMethod.EWALLET -> if (selectedMethodName == null) showEwalletOptions()
                PaymentMethod.COD -> selectedMethodName = "COD"
                null -> {}
            }

            val method = selectedMethodName
            if (method.isNullOrBlank()) return@setOnClickListener

            // Use numeric total for backend
            val totalInt = presenter.computeSummary().total
            // Prefer intent extra; if empty, use saved user address
            var addressFromPrev = intent.getStringExtra("shippingAddress") ?: ""
            if (addressFromPrev.isBlank()) {
                val saved = AuthManager(this).getCurrentUser()
                addressFromPrev = saved?.address.orEmpty()
            }

            fun goNext() {
                val i = android.content.Intent(this, OrderSummaryActivity::class.java)
                i.putExtra("totalAmountInt", totalInt)
                i.putExtra("totalPay", CurrencyFormatter.formatRupiah(totalInt))
                i.putExtra("shippingAddress", addressFromPrev)
                i.putExtra("method", method)
                startActivity(i)
                finish()
            }

            if (addressFromPrev.isBlank()) {
                // Ask user to input shipping address first
                val input = android.widget.EditText(this)
                input.hint = "Alamat pengiriman"
                input.minLines = 2
                input.maxLines = 4
                AlertDialog.Builder(this)
                        .setTitle("Alamat Pengiriman")
                        .setMessage("Masukkan alamat pengiriman Anda")
                        .setView(input)
                        .setPositiveButton("Simpan") { d, _ ->
                            val v = input.text?.toString()?.trim().orEmpty()
                            if (v.isNotEmpty()) {
                                addressFromPrev = v
                                goNext()
                            }
                            d.dismiss()
                        }
                        .setNegativeButton("Batal", null)
                        .show()
            } else {
                goNext()
            }
        }

        fun select(m: PaymentMethod) {
            presenter.selectMethod(m)
            rbBank.isChecked = m == PaymentMethod.BANK
            rbEwallet.isChecked = m == PaymentMethod.EWALLET
            rbCod.isChecked = m == PaymentMethod.COD
            // reset previously chosen provider when switching
            selectedMethodName =
                    when (m) {
                        PaymentMethod.COD -> "COD"
                        else -> null
                    }
            if (m == PaymentMethod.BANK) showBankOptions()
            if (m == PaymentMethod.EWALLET) showEwalletOptions()
        }
        findViewById<View>(R.id.cardBank).setOnClickListener { select(PaymentMethod.BANK) }
        findViewById<View>(R.id.cardEwallet).setOnClickListener { select(PaymentMethod.EWALLET) }
        findViewById<View>(R.id.cardCod).setOnClickListener { select(PaymentMethod.COD) }
    }
}
