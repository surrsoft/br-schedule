package com.example.brschedule

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ScheduleAdapter : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    private var offset: Int = 0
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("d MMM, E", Locale("ru"))
    private val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    fun setOffset(newOffset: Int) {
        offset = newOffset
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule_day, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, position - 5000)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        holder.tvDate.text = dateFormat.format(date.time)

        val daysSinceEpoch = (date.timeInMillis / (24 * 60 * 60 * 1000)).toInt()
        val scheduleIndex = (daysSinceEpoch + offset) % 4
        val scheduleType = ScheduleType.fromIndex(scheduleIndex)

        holder.tvLabel.text = scheduleType.label
        holder.ivIcon.setImageResource(scheduleType.iconRes)
        holder.ivIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, scheduleType.colorRes))
        holder.tvTimeRange.text = scheduleType.timeRange ?: ""

        if (date == today) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.today_highlight).let {
                Color.argb(40, Color.red(it), Color.green(it), Color.blue(it))
            })
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount(): Int = 10000 // Infinite-like scrolling

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        val tvLabel: TextView = view.findViewById(R.id.tvLabel)
        val tvTimeRange: TextView = view.findViewById(R.id.tvTimeRange)
    }
}
