package com.example.appprojek.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
        val id: String,
        val name: String,
        val priceRupiah: Int,
        val imageResId: Int? = null,
        val description: String = ""
) : Parcelable
