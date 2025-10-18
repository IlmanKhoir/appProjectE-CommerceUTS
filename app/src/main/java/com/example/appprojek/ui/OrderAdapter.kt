package com.example.appprojek.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appprojek.R
import com.example.appprojek.domain.OrderStatus
import com.example.appprojek.model.Order
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(private var orders: List<Order>, private val onOrderClick: (Order) -> Unit) :
        RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun getItemCount(): Int = orders.size

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
        holder.itemView.setOnClickListener { onOrderClick(order) }
    }

    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderIdText: TextView = itemView.findViewById(R.id.tvOrderId)
        private val orderDateText: TextView = itemView.findViewById(R.id.tvOrderDate)
        private val totalAmountText: TextView = itemView.findViewById(R.id.tvTotalAmount)
        private val statusText: TextView = itemView.findViewById(R.id.tvStatus)
        private val itemCountText: TextView = itemView.findViewById(R.id.tvItemCount)
        private val titleText: TextView? = itemView.findViewById(R.id.tvOrderTitle)

        fun bind(order: Order) {
            orderIdText.text = "Pesanan #${order.orderId}"
            orderDateText.text = formatDate(order.orderDate)
            totalAmountText.text = formatRupiah(order.totalAmount)
            statusText.text = getStatusText(order.status)
            statusText.setTextColor(getStatusColor(order.status))
            itemCountText.text = "${order.items.size} item"

            // Set judul: nama item pertama + jumlah lainnya
            val firstName = order.items.firstOrNull()?.product?.name
            val others = (order.items.size - 1).coerceAtLeast(0)
            val title = if (firstName != null) {
                if (others > 0) "$firstName + $others lainnya" else firstName
            } else "Pesanan"
            titleText?.text = title
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
            return sdf.format(Date(timestamp))
        }

        private fun formatRupiah(amount: Int): String {
            return "Rp %,d".format(amount).replace(',', '.')
        }

        private fun getStatusText(status: OrderStatus): String {
            return when (status) {
                OrderStatus.PENDING -> "Menunggu Konfirmasi"
                OrderStatus.CONFIRMED -> "Dikonfirmasi"
                OrderStatus.PROCESSING -> "Sedang Diproses"
                OrderStatus.SHIPPED -> "Sedang Dikirim"
                OrderStatus.DELIVERED -> "Selesai"
                OrderStatus.CANCELLED -> "Dibatalkan"
            }
        }

        private fun getStatusColor(status: OrderStatus): Int {
            return when (status) {
                OrderStatus.PENDING -> itemView.context.getColor(R.color.orange)
                OrderStatus.CONFIRMED -> itemView.context.getColor(R.color.primary)
                OrderStatus.PROCESSING -> itemView.context.getColor(R.color.primary)
                OrderStatus.SHIPPED -> itemView.context.getColor(R.color.primary)
                OrderStatus.DELIVERED -> itemView.context.getColor(R.color.green)
                OrderStatus.CANCELLED -> itemView.context.getColor(R.color.red)
            }
        }
    }
}
