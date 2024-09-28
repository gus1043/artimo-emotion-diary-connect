package com.example.artimo_emotion_diary

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarAdapter(
    private val context: Context,
    private val itemList: MutableList<Date>,  // MutableList로 변경하여 아이템 리스트 저장
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(date: Date)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.date)
        private val emojiImageView: ImageView = itemView.findViewById(R.id.emoji)

        fun bind(date: Date) {
            Log.d("CalendarAdapter", "바인딩한 date: ${date.date}, emoji: ${date.emoji}")
            dateTextView.text = if (date.date > 0) date.date.toString() else ""
            loadEmoji(date.emoji)

            itemView.setOnClickListener {
                Log.d("CalendarAdapter", "클릭된 item: ${date.date}")
                onItemClickListener.onItemClick(date)
            }
        }

        private fun loadEmoji(emojiFileName: String?) {
            Log.d("CalendarAdapter", "이모지: $emojiFileName")
            if (!emojiFileName.isNullOrEmpty()) {
                try {
                    if (emojiFileName != "null") {
                        context.assets.open(emojiFileName).use { inputStream ->
                            val drawable = Drawable.createFromStream(inputStream, null)
                            emojiImageView.setImageDrawable(drawable)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("CalendarAdapter", "이모지 에러: $emojiFileName", e)
                    emojiImageView.setImageResource(R.drawable.logo)
                }
            } else {
                emojiImageView.setImageResource(R.drawable.logo)
                emojiImageView.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_cell, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        Log.d("CalendarAdapter", "Item count: ${itemList.size}")
        return itemList.size
    }
}
