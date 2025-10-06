package com.example.appprojek

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OrderSummaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_summary)

        val toolbar =
                findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val total = intent.getStringExtra("totalPay") ?: "Rp 0"
        val method = intent.getStringExtra("method") ?: "-"
        findViewById<TextView>(R.id.textMethod).text = method
        findViewById<TextView>(R.id.textTotal).text = total

        val layoutVa = findViewById<android.view.View>(R.id.layoutVa)
        val textVa = findViewById<TextView>(R.id.textVa)
        val buttonCopy = findViewById<Button>(R.id.buttonCopyVa)
        if (method.contains("Transfer", ignoreCase = true)) {
            layoutVa.visibility = android.view.View.VISIBLE
            val va = generateRandomVa()
            textVa.text = va
            buttonCopy.setOnClickListener {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("VA", va))
                android.widget.Toast.makeText(
                                this,
                                getString(R.string.copied),
                                android.widget.Toast.LENGTH_SHORT
                        )
                        .show()
            }
        } else {
            layoutVa.visibility = android.view.View.GONE
        }

        findViewById<Button>(R.id.buttonConfirm).setOnClickListener {
            startActivity(android.content.Intent(this, ShippingActivity::class.java))
            finish()
        }

        val textCountdown = findViewById<TextView>(R.id.textCountdown)
        object : CountDownTimer(15 * 60 * 1000L, 1000L) {
                    override fun onTick(millisUntilFinished: Long) {
                        val m = (millisUntilFinished / 1000) / 60
                        val s = (millisUntilFinished / 1000) % 60
                        textCountdown.text = String.format("Sisa waktu bayar: %02d:%02d", m, s)
                    }
                    override fun onFinish() {
                        textCountdown.text = "Waktu pembayaran habis"
                    }
                }
                .start()
    }

    private fun generateRandomVa(): String {
        val prefix = "8808"
        val rand = (100000000..999999999).random()
        return prefix + rand.toString()
    }
}
