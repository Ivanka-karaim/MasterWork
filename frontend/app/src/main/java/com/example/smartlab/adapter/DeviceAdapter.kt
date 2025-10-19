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
import com.example.smartlab.R
import com.example.smartlab.addResources.RetrofitClient
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.api.ManagementApi
import com.example.smartlab.model.DeviceResponse
import com.example.smartlab.ManagementActivity
import com.example.smartlab.addResources.ErrorHandler
import com.example.smartlab.service.ManagementService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class DeviceAdapter(private val devices: List<DeviceResponse>,
                    private val context: Context,
                    private val scope: CoroutineScope,
                    private val error: TextView,
                    private val type: String,
                    private val role: String
) :
    RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.deviceImage)
        val title: TextView = view.findViewById(R.id.deviceTitle)
        val switch: Switch = view.findViewById(R.id.deviceSwitch)
        val arrow: ImageView = view.findViewById(R.id.expandArrow)
        val detailsLayout: View = view.findViewById(R.id.detailsLayout)
        val description: TextView = view.findViewById(R.id.deviceDescription)
        val inventoryNumber: TextView = view.findViewById(R.id.deviceInventoryNumber)
        val eventLog: LinearLayout = view.findViewById(R.id.eventLog)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]

        holder.title.text = device.title
        holder.description.text = device.description
        holder.inventoryNumber.text = "Інвентарний номер: ${device.inventoryNumber}"

        holder.switch.setOnCheckedChangeListener(null)
        holder.switch.isChecked = device.on

        if(role == "STUDENT"){
            holder.switch.isEnabled  = false
        } else{
            holder.switch.isEnabled  = true
        }
        setDeviceImage(holder, device.image)
        setupArrow(holder)
        setupSwitch(holder, device)

        holder.eventLog.setOnClickListener {
            openEventLog(device.id)
        }
    }

    private fun setDeviceImage(holder: DeviceViewHolder, imageBase64: String) {
        val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        holder.image.setImageBitmap(bitmap)
    }

    private fun setupArrow(holder: DeviceViewHolder) {
        holder.arrow.setOnClickListener {
            val isVisible = holder.detailsLayout.visibility == View.VISIBLE
            holder.detailsLayout.visibility = if (isVisible) View.GONE else View.VISIBLE
            holder.arrow.animate().rotation(if (isVisible) 0f else 180f).setDuration(200).start()
        }
    }

    private fun setupSwitch(holder: DeviceViewHolder, device: DeviceResponse) {
        holder.switch.setOnCheckedChangeListener { _, isChecked ->
            onDeviceSwitchChanged(holder, device, isChecked)
        }
    }


    private fun onDeviceSwitchChanged(holder: DeviceViewHolder, device: DeviceResponse, isChecked: Boolean) {
        val actionType = if (isChecked) "ON" else "OFF"
        val managementApi = RetrofitClient.getInstance().create(ManagementApi::class.java)
        val token = SharedPreferencesFactory(context).getSharedPreferences("TOKEN")!!
        scope.launch {

            val errors = ManagementService.createNewAction(managementApi, device.id , actionType, 0.0,
                "Bearer $token"
            )
            if (errors.isEmpty()) {

            } else if (errors == "unknownError") {
                ErrorHandler.generalError(context)
            } else if (errors == "unauthorized") {
                if (context is Activity) {
                    ErrorHandler.unauthorizedUser(context)
                }
            }
            else {
                error.text = errors
                error.visibility = View.VISIBLE

                Handler(Looper.getMainLooper()).postDelayed({
                    error.visibility = View.GONE
                }, 3000)
            }
        }
    }

    private fun openEventLog(deviceId: String) {
        val intent = Intent(context, ManagementActivity::class.java)
        intent.putExtra("DEVICE_ID", deviceId)
        context.startActivity(intent)
    }

    override fun getItemCount(): Int = devices.size
}
