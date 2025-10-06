package com.example.appprojek.util

object CurrencyFormatter {
    fun formatRupiah(amount: Int): String {
        return "Rp %,d".format(amount).replace(',', '.')
    }
}


