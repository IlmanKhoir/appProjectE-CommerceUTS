package com.example.appprojek.domain

import com.example.appprojek.cart.CartItem
import com.example.appprojek.cart.CartManager
import com.example.appprojek.model.Product

interface ICartService {
    fun add(product: Product)
    fun removeOne(productId: String)
    fun clear()
    fun getItems(): List<CartItem>
    fun getSubtotal(): Int
}

class CartServiceAdapter : ICartService {
    override fun add(product: Product) = CartManager.add(product)
    override fun removeOne(productId: String) = CartManager.removeOne(productId)
    override fun clear() = CartManager.clear()
    override fun getItems(): List<CartItem> = CartManager.getItems()
    override fun getSubtotal(): Int = CartManager.getTotalRupiah()
}


