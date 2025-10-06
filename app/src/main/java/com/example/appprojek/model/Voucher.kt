package com.example.appprojek.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Voucher(
        val id: String,
        val code: String,
        val name: String,
        val description: String,
        val discountType: DiscountType,
        val discountValue: Int, // percentage or fixed amount
        val minOrderAmount: Int = 0,
        val maxDiscountAmount: Int? = null,
        val validFrom: Long,
        val validUntil: Long,
        val isActive: Boolean = true,
        val usageLimit: Int? = null, // null means unlimited
        val usedCount: Int = 0
) : Parcelable

enum class DiscountType {
    PERCENTAGE,
    FIXED_AMOUNT,
    FREE_SHIPPING
}
