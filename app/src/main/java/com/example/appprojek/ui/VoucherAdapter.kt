package com.example.appprojek.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appprojek.R
import com.example.appprojek.model.DiscountType
import com.example.appprojek.model.Voucher
import java.text.SimpleDateFormat
import java.util.*

class VoucherAdapter(
        private var vouchers: List<Voucher>,
        private val onVoucherClick: (Voucher) -> Unit
) : RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_voucher, parent, false)
        return VoucherViewHolder(view)
    }

    override fun getItemCount(): Int = vouchers.size

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        val voucher = vouchers[position]
        holder.bind(voucher)
        holder.itemView.setOnClickListener { onVoucherClick(voucher) }
    }

    fun updateVouchers(newVouchers: List<Voucher>) {
        vouchers = newVouchers
        notifyDataSetChanged()
    }

    class VoucherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val codeText: TextView = itemView.findViewById(R.id.tvVoucherCode)
        private val nameText: TextView = itemView.findViewById(R.id.tvVoucherName)
        private val descriptionText: TextView = itemView.findViewById(R.id.tvVoucherDescription)
        private val discountText: TextView = itemView.findViewById(R.id.tvDiscount)
        private val validUntilText: TextView = itemView.findViewById(R.id.tvValidUntil)
        private val minOrderText: TextView = itemView.findViewById(R.id.tvMinOrder)

        fun bind(voucher: Voucher) {
            codeText.text = voucher.code
            nameText.text = voucher.name
            descriptionText.text = voucher.description
            validUntilText.text = "Berlaku hingga ${formatDate(voucher.validUntil)}"
            minOrderText.text = "Min. pembelian: ${formatRupiah(voucher.minOrderAmount)}"

            // Set discount text based on type
            discountText.text =
                    when (voucher.discountType) {
                        DiscountType.PERCENTAGE -> "${voucher.discountValue}% OFF"
                        DiscountType.FIXED_AMOUNT -> "${formatRupiah(voucher.discountValue)} OFF"
                        DiscountType.FREE_SHIPPING -> "GRATIS ONGKIR"
                    }

            // Set text color based on discount type
            val discountColor =
                    when (voucher.discountType) {
                        DiscountType.PERCENTAGE -> itemView.context.getColor(R.color.orange)
                        DiscountType.FIXED_AMOUNT -> itemView.context.getColor(R.color.green)
                        DiscountType.FREE_SHIPPING -> itemView.context.getColor(R.color.primary)
                    }
            discountText.setTextColor(discountColor)
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            return sdf.format(Date(timestamp))
        }

        private fun formatRupiah(amount: Int): String {
            return "Rp %,d".format(amount).replace(',', '.')
        }
    }
}
