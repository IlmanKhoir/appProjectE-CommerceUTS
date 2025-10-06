package com.example.appprojek.presentation

import com.example.appprojek.domain.ICartService
import com.example.appprojek.domain.PaymentMethod

data class PaymentSummary(
    val subtotal: Int,
    val shipping: Int,
    val discount: Int,
    val total: Int
)

class PaymentPresenter(private val cartService: ICartService) {
    var selectedMethod: PaymentMethod? = null
        private set
    private var discount: Int = 0

    fun selectMethod(method: PaymentMethod) { selectedMethod = method }

    fun applyVoucher(code: String) {
        val subtotal = cartService.getSubtotal()
        discount = if (code.equals("DUANAK10", ignoreCase = true)) (subtotal * 0.1).toInt() else 0
    }

    fun computeSummary(): PaymentSummary {
        val subtotal = cartService.getSubtotal()
        val shipping = if (subtotal >= 50000) 0 else 10000
        val total = (subtotal + shipping - discount).coerceAtLeast(0)
        return PaymentSummary(subtotal, shipping, discount, total)
    }
}


