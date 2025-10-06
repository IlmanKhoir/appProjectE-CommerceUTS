package com.example.appprojek.model

import android.os.Parcel
import android.os.Parcelable

data class ChatMessage(
        val id: String,
        val message: String,
        val timestamp: Long = System.currentTimeMillis(),
        val isFromUser: Boolean = true,
        val senderName: String = "",
        val messageType: MessageType = MessageType.TEXT
) : Parcelable {

    constructor(
            parcel: Parcel
    ) : this(
            id = requireNotNull(parcel.readString()) { "id cannot be null" },
            message = requireNotNull(parcel.readString()) { "message cannot be null" },
            timestamp = parcel.readLong(),
            isFromUser = parcel.readByte() != 0.toByte(),
            senderName = requireNotNull(parcel.readString()) { "senderName cannot be null" },
            messageType = MessageType.values()[parcel.readInt()]
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(message)
        parcel.writeLong(timestamp)
        parcel.writeByte(if (isFromUser) 1 else 0)
        parcel.writeString(senderName)
        parcel.writeInt(messageType.ordinal)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ChatMessage> {
        override fun createFromParcel(parcel: Parcel): ChatMessage = ChatMessage(parcel)
        override fun newArray(size: Int): Array<ChatMessage?> = arrayOfNulls(size)
    }
}

enum class MessageType {
    TEXT,
    IMAGE,
    SYSTEM
}
