package com.example.appprojek.model

import android.os.Parcel
import android.os.Parcelable

data class Notification(
        val id: String,
        val title: String,
        val message: String,
        val type: NotificationType,
        val timestamp: Long = System.currentTimeMillis(),
        val isRead: Boolean = false,
        val actionUrl: String? = null
) : Parcelable {
    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(message)
        parcel.writeString(type.name)
        parcel.writeLong(timestamp)
        parcel.writeByte(if (isRead) 1 else 0)
        parcel.writeString(actionUrl)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Notification> =
                object : Parcelable.Creator<Notification> {
                    override fun createFromParcel(parcel: Parcel): Notification {
                        val id = parcel.readString() ?: ""
                        val title = parcel.readString() ?: ""
                        val message = parcel.readString() ?: ""
                        val typeName = parcel.readString() ?: NotificationType.GENERAL.name
                        val type =
                                try {
                                    NotificationType.valueOf(typeName)
                                } catch (_: IllegalArgumentException) {
                                    NotificationType.GENERAL
                                }
                        val timestamp = parcel.readLong()
                        val isRead = parcel.readByte().toInt() != 0
                        val actionUrl = parcel.readString()
                        return Notification(
                                id = id,
                                title = title,
                                message = message,
                                type = type,
                                timestamp = timestamp,
                                isRead = isRead,
                                actionUrl = actionUrl
                        )
                    }

                    override fun newArray(size: Int): Array<Notification?> = arrayOfNulls(size)
                }
    }
}

enum class NotificationType {
    PROMO,
    ORDER_UPDATE,
    GENERAL,
    SYSTEM
}
