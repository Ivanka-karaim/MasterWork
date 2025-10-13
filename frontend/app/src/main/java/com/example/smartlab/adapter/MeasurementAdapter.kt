package com.example.smartlab.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartlab.R
import com.example.smartlab.model.Measurement
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MeasurementAdapter(private var list: List<Measurement>) :
    RecyclerView.Adapter<MeasurementAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val value: TextView = view.findViewById(R.id.value)
        val date: TextView = view.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_measurement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.value.text = String.format("%.3f", item.value)
        val sdf = SimpleDateFormat("dd.MM.yyyy    HH:mm", Locale.getDefault())
        holder.date.text = formatIsoToDisplay(item.dateTime)
    }

    override fun getItemCount() = list.size
    fun setData(newMeasurements: List<Measurement>) {
        list = newMeasurements
        notifyDataSetChanged()
    }
    fun formatIsoToDisplay(dateString: String?): String {
        if (dateString.isNullOrBlank()) return ""

        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss"
        )

        for (format in formats) {
            try {
                val parser = SimpleDateFormat(format, Locale.getDefault())
                val date: Date = parser.parse(dateString) ?: continue
                val displayFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                return displayFormat.format(date)
            } catch (e: ParseException) {

            }
        }

        return ""
    }
}
