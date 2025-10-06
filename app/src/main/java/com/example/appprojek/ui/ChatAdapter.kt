package com.example.appprojek.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appprojek.R
import com.example.appprojek.model.ChatMessage
import com.example.appprojek.model.MessageType
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val messages: List<ChatMessage>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2
        private const val VIEW_TYPE_SYSTEM = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (messages[position].messageType) {
            MessageType.SYSTEM -> VIEW_TYPE_SYSTEM
            MessageType.TEXT, MessageType.IMAGE -> {
                if (messages[position].isFromUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view =
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_chat_user, parent, false)
                UserMessageViewHolder(view)
            }
            VIEW_TYPE_BOT -> {
                val view =
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_chat_bot, parent, false)
                BotMessageViewHolder(view)
            }
            VIEW_TYPE_SYSTEM -> {
                val view =
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_chat_system, parent, false)
                SystemMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is BotMessageViewHolder -> holder.bind(message)
            is SystemMessageViewHolder -> holder.bind(message)
        }
    }

    class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tvMessage)
        private val timeText: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(message: ChatMessage) {
            messageText.text = message.message
            timeText.text = formatTime(message.timestamp)
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    class BotMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tvMessage)
        private val timeText: TextView = itemView.findViewById(R.id.tvTime)
        private val senderText: TextView = itemView.findViewById(R.id.tvSender)

        fun bind(message: ChatMessage) {
            messageText.text = message.message
            timeText.text = formatTime(message.timestamp)
            senderText.text = message.senderName
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    class SystemMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tvMessage)

        fun bind(message: ChatMessage) {
            messageText.text = message.message
        }
    }
}
