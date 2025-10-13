package com.example.smartlab.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartlab.ManagementActivity
import com.example.smartlab.R
import com.example.smartlab.SensorDataActivity
import com.example.smartlab.addResources.ErrorHandler
import com.example.smartlab.addResources.RetrofitClient
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.api.ManagementApi
import com.example.smartlab.model.DeviceResponse
import com.example.smartlab.service.ManagementService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SensorAdapter(private val sensors: List<DeviceResponse>,
                    private val context: Context
) :
    RecyclerView.Adapter<SensorAdapter.SensorViewHolder>() {

    inner class SensorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.deviceImage)
        val title: TextView = view.findViewById(R.id.deviceTitle)
        val description: TextView = view.findViewById(R.id.deviceDescription)
        val inventoryNumber: TextView = view.findViewById(R.id.deviceInventoryNumber)
        val management: LinearLayout = view.findViewById(R.id.management)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sensor, parent, false)
        return SensorViewHolder(view)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        val sensor = sensors[position]

        holder.title.text = sensor.title
        holder.description.text = shortenText(sensor.description, 50)
        holder.inventoryNumber.text = "Інвентарний номер: ${sensor.inventoryNumber}"

        setDeviceImage(holder, sensor.image)


        holder.management.setOnClickListener {
            openEventLog(sensor.id)
        }
    }

    private fun shortenText(text: String?, maxLength: Int): String {
        if (text.isNullOrEmpty()) return ""
        return if (text.length > maxLength) text.take(maxLength) + "…" else text
    }

    private fun setDeviceImage(holder: SensorViewHolder, imageBase64: String) {
        val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        holder.image.setImageBitmap(bitmap)
    }


    private fun openEventLog(deviceId: String) {
        val intent = Intent(context, SensorDataActivity::class.java)
        intent.putExtra("DEVICE_ID", deviceId)
        context.startActivity(intent)
    }

    override fun getItemCount(): Int = sensors.size
}