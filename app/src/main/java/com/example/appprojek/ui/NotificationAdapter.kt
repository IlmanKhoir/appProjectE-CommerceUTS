package com.example.appprojek.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appprojek.R
import com.example.appprojek.model.Notification
import com.example.appprojek.model.NotificationType
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
        private var notifications: List<Notification>,
        private val onNotificationClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view =
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun getItemCount(): Int = notifications.size

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
        holder.itemView.setOnClickListener { onNotificationClick(notification) }
    }

    fun updateNotifications(newNotifications: List<Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }

    fun markAllAsRead() {
        notifications = notifications.map { it.copy(isRead = true) }
        notifyDataSetChanged()
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.tvTitle)
        private val messageText: TextView = itemView.findViewById(R.id.tvMessage)
        private val timeText: TextView = itemView.findViewById(R.id.tvTime)
        private val typeIndicator: View = itemView.findViewById(R.id.typeIndicator)

        fun bind(notification: Notification) {
            titleText.text = notification.title
            messageText.text = notification.message
            timeText.text = formatTime(notification.timestamp)

            // Set background color based on read status
            if (notification.isRead) {
                itemView.setBackgroundColor(itemView.context.getColor(R.color.white))
            } else {
                itemView.setBackgroundColor(itemView.context.getColor(R.color.gray_light))
            }

            // Set indicator color based on notification type
            val indicatorColor =
                    when (notification.type) {
                        NotificationType.PROMO -> itemView.context.getColor(R.color.orange)
                        NotificationType.ORDER_UPDATE -> itemView.context.getColor(R.color.primary)
                        NotificationType.GENERAL -> itemView.context.getColor(R.color.gray)
                        NotificationType.SYSTEM -> itemView.context.getColor(R.color.green)
                    }
            typeIndicator.setBackgroundColor(indicatorColor)
        }

        private fun formatTime(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60000 -> "Baru saja"
                diff < 3600000 -> "${diff / 60000} menit lalu"
                diff < 86400000 -> "${diff / 3600000} jam lalu"
                else -> {
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                    sdf.format(Date(timestamp))
                }
            }
        }
    }
}
