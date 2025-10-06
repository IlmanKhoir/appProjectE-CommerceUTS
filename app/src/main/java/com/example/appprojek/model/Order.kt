package com.example.appprojek.model

import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat
import com.example.appprojek.domain.OrderStatus

data class Order(
        val orderId: String,
        val userId: String,
        val items: List<CartItem>,
        val totalAmount: Int,
        val status: OrderStatus,
        val orderDate: Long = System.currentTimeMillis(),
        val shippingAddress: String = "",
        val paymentMethod: String = "",
        val trackingNumber: String? = null
) : Parcelable {

    constructor(
            parcel: Parcel
    ) : this(
            orderId = requireNotNull(parcel.readString()) { "orderId cannot be null" },
            userId = requireNotNull(parcel.readString()) { "userId cannot be null" },
            items =
                    requireNotNull(parcel.createTypedArrayList(CartItem.CREATOR)) {
                        "items cannot be null"
                    },
            totalAmount = parcel.readInt(),
            status = OrderStatus.values()[parcel.readInt()],
            orderDate = parcel.readLong(),
            shippingAddress =
                    requireNotNull(parcel.readString()) { "shippingAddress cannot be null" },
            paymentMethod = requireNotNull(parcel.readString()) { "paymentMethod cannot be null" },
            trackingNumber = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(orderId)
        parcel.writeString(userId)
        parcel.writeTypedList(items)
        parcel.writeInt(totalAmount)
        parcel.writeInt(status.ordinal)
        parcel.writeLong(orderDate)
        parcel.writeString(shippingAddress)
        parcel.writeString(paymentMethod)
        parcel.writeString(trackingNumber)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Order> {
        override fun createFromParcel(parcel: Parcel): Order = Order(parcel)
        override fun newArray(size: Int): Array<Order?> = arrayOfNulls(size)
    }
}

data class CartItem(val product: Product, val quantity: Int) : Parcelable {

    constructor(
            parcel: Parcel
    ) : this(
            product =
                    requireNotNull(
                            ParcelCompat.readParcelable(
                                    parcel,
                                    Product::class.java.classLoader,
                                    Product::class.java
                            )
                    ) { "product cannot be null" },
            quantity = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(product, flags)
        parcel.writeInt(quantity)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CartItem> {
        override fun createFromParcel(parcel: Parcel): CartItem = CartItem(parcel)
        override fun newArray(size: Int): Array<CartItem?> = arrayOfNulls(size)
    }
}
