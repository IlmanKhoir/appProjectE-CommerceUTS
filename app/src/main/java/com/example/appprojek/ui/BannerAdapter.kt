package com.example.appprojek.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appprojek.R

data class BannerItem(val title: String, val imageRes: Int)

class BannerAdapter(private val items: List<BannerItem>) :
        RecyclerView.Adapter<BannerAdapter.VH>() {
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgBanner)
        val text: TextView = view.findViewById(R.id.textBanner)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_banner, parent, false)
        return VH(v)
    }
    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.text.text = item.title
        holder.img.setImageResource(item.imageRes)
    }
}
