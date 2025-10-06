package com.example.appprojek.cart

import com.example.appprojek.model.Product

object CartManager {
    private val productIdToQuantity: MutableMap<String, Int> = mutableMapOf()
    private val productIdToProduct: MutableMap<String, Product> = mutableMapOf()

    fun add(product: Product) {
        productIdToProduct[product.id] = product
        val current = productIdToQuantity[product.id] ?: 0
        productIdToQuantity[product.id] = current + 1
    }

    fun removeOne(productId: String) {
        val current = productIdToQuantity[productId] ?: return
        if (current <= 1) {
            productIdToQuantity.remove(productId)
            productIdToProduct.remove(productId)
        } else {
            productIdToQuantity[productId] = current - 1
        }
    }

    fun clear() {
        productIdToQuantity.clear()
        productIdToProduct.clear()
    }

    fun getItems(): List<CartItem> {
        return productIdToQuantity.mapNotNull { (id, qty) ->
            val product = productIdToProduct[id]
            product?.let { CartItem(it, qty) }
        }
    }

    fun getTotalRupiah(): Int {
        return getItems().sumOf { it.product.priceRupiah * it.quantity }
    }
}

data class CartItem(
    val product: Product,
    val quantity: Int
)


