package com.example.appprojek

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.appprojek.domain.CartServiceAdapter
import com.example.appprojek.domain.PaymentMethod
import com.example.appprojek.presentation.PaymentPresenter
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

        val payButton = findViewById<Button>(R.id.buttonConfirmPay)
        payButton.setOnClickListener {
            val method =
                    when (presenter.selectedMethod) {
                        PaymentMethod.BANK -> getString(R.string.payment_bank)
                        PaymentMethod.EWALLET -> getString(R.string.payment_ewallet)
                        PaymentMethod.COD -> getString(R.string.payment_cod)
                        null -> null
                    }
            if (method != null) {
                val total = textTotalPay.text.toString()
                val intent = android.content.Intent(this, OrderSummaryActivity::class.java)
                intent.putExtra("totalPay", total)
                intent.putExtra("method", method)
                startActivity(intent)
                finish()
            }
        }

        fun select(m: PaymentMethod) {
            presenter.selectMethod(m)
            rbBank.isChecked = m == PaymentMethod.BANK
            rbEwallet.isChecked = m == PaymentMethod.EWALLET
            rbCod.isChecked = m == PaymentMethod.COD
        }
        findViewById<View>(R.id.cardBank).setOnClickListener { select(PaymentMethod.BANK) }
        findViewById<View>(R.id.cardEwallet).setOnClickListener { select(PaymentMethod.EWALLET) }
        findViewById<View>(R.id.cardCod).setOnClickListener { select(PaymentMethod.COD) }
    }
}
