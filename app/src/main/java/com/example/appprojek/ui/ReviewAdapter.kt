package com.example.appprojek.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appprojek.R
import com.example.appprojek.model.Review
import java.text.SimpleDateFormat
import java.util.*

class ReviewAdapter(private var reviews: List<Review>) :
        RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun getItemCount(): Int = reviews.size

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.bind(review)
    }

    fun updateReviews(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userNameText: TextView = itemView.findViewById(R.id.tvUserName)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val commentText: TextView = itemView.findViewById(R.id.tvComment)
        private val dateText: TextView = itemView.findViewById(R.id.tvDate)
        private val helpfulText: TextView = itemView.findViewById(R.id.tvHelpful)

        fun bind(review: Review) {
            userNameText.text = review.userName
            ratingBar.rating = review.rating.toFloat()
            commentText.text = review.comment
            dateText.text = formatDate(review.date)
            helpfulText.text = "${review.helpful} orang merasa review ini membantu"
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
            return sdf.format(Date(timestamp))
        }
    }
}
