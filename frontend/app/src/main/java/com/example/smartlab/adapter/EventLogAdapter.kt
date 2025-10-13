package com.example.smartlab.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartlab.R
import com.example.smartlab.model.DeviceLog
import java.text.SimpleDateFormat
import java.util.Locale

class EventLogAdapter(private val logs: List<DeviceLog>)
    : RecyclerView.Adapter<EventLogAdapter.DeviceViewHolder>()  {

    inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAction: TextView = view.findViewById(R.id.tvAction)
        val tvValue: TextView = view.findViewById(R.id.tvValue)
        val tvDateTime: TextView = view.findViewById(R.id.tvDateTime)
        val tvPerformer: TextView = view.findViewById(R.id.tvPerformer)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventLogAdapter.DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event_log, parent, false)
        return DeviceViewHolder(view)
    }

    override fun getItemCount(): Int = logs.size

    override fun onBindViewHolder(holder: EventLogAdapter.DeviceViewHolder, position: Int) {
        val log = logs[position]

        holder.tvAction.text = log.actionType
        holder.tvValue.text = log.actionValue.toString()
        holder.tvPerformer.text = log.userName ?: ""


        // Форматуємо дату
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        try {
            val date = inputFormat.parse(log.dateTime)
            holder.tvDateTime.text = if (date != null) outputFormat.format(date) else log.dateTime
        } catch (e: Exception) {
            holder.tvDateTime.text = log.dateTime // fallback, якщо парсинг не вдався
        }
    }

}