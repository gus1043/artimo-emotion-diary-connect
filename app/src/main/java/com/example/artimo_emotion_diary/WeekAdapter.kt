package com.example.artimo_emotion_diary

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

class WeekAdapter(private val daysOfWeek: List<DayOfWeek>) :
    RecyclerView.Adapter<WeekAdapter.WeekViewHolder>() {

    class WeekViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayNameTextView: TextView = view.findViewById(R.id.day_cell)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_week, parent, false)
        return WeekViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeekViewHolder, position: Int) {
        val dayOfWeek = daysOfWeek[position]
        holder.dayNameTextView.text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)

        // sun, sat 색깔 설정
        when (dayOfWeek) {
            DayOfWeek.SUNDAY -> holder.dayNameTextView.setTextColor(Color.parseColor("#d6674c"))
            DayOfWeek.SATURDAY -> holder.dayNameTextView.setTextColor(Color.parseColor("#618dd3"))
            else -> holder.dayNameTextView.setTextColor(Color.BLACK)
        }
    }

    override fun getItemCount() = daysOfWeek.size
}
