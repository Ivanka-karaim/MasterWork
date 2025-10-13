package com.example.smartlab.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartlab.R
import com.example.smartlab.adapter.DeviceAdapter.DeviceViewHolder
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.model.NotificationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationAdapter(
    private val onClick: (NotificationData) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private var notifications = mutableListOf<NotificationData>()

    fun setData(data: List<NotificationData>) {
        notifications = data.toMutableList()
        notifyDataSetChanged()
    }

    fun addNotification(notification: NotificationData) {
        notifications.add(0, notification)
        notifyItemInserted(0)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.titleText)
        val message = view.findViewById<TextView>(R.id.messageText)
        val badge = view.findViewById<View>(R.id.unreadBadge)
        val dateTime = view.findViewById<TextView>(R.id.dateTimeText)
        val image = view.findViewById<ImageView>(R.id.deviceImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notif = notifications[position]
        holder.title.text = shortenText(notif.title, 20)
        holder.message.text = shortenText(notif.message, 75)
        holder.badge.visibility = if (!notif.read) View.VISIBLE else View.GONE


        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        try {
            val date = inputFormat.parse(notif.dateTime)
            holder.dateTime.text = if (date != null) outputFormat.format(date) else notif.dateTime
        } catch (e: Exception) {
            holder.dateTime.text = notif.dateTime
        }

        if(notif.device != null) {
            setDeviceImage(holder, notif.device.image)
        } else{
            holder.image.setImageResource(R.drawable.ic_new_notification)
        }


        holder.view.setOnClickListener { showNotificationDetail(holder.view, notif) }
    }

    private fun shortenText(text: String?, maxLength: Int): String {
        if (text.isNullOrEmpty()) return ""
        return if (text.length > maxLength) text.take(maxLength) + "…" else text
    }

    private fun setDeviceImage(holder: ViewHolder, imageBase64: String) {
        val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        holder.image.setImageBitmap(bitmap)
    }

    private fun showNotificationDetail(parentView: View, notif: NotificationData) {
        val context = parentView.context
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_notification_detail, null)

        val imageView = dialogView.findViewById<ImageView>(R.id.deviceImage)
        val titleView = dialogView.findViewById<TextView>(R.id.titleText)
        val messageView = dialogView.findViewById<TextView>(R.id.messageText)
        val titleDevice = dialogView.findViewById<TextView>(R.id.titleDevice)
        val descriptionDevice = dialogView.findViewById<TextView>(R.id.descriptionDevice)
        val inventoryDevice = dialogView.findViewById<TextView>(R.id.inventoryNumberDevice)
        val closeButton = dialogView.findViewById<ImageView>(R.id.closeButton)

        titleView.text = notif.title
        messageView.text = notif.message

        if(notif.device != null) {
            val imageBytes = Base64.decode(notif.device.image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageView.setImageBitmap(bitmap)

            titleDevice.text = notif.device.title
            descriptionDevice.text = notif.device.description
            inventoryDevice.text = notif.device.inventoryNumber
        } else {
            imageView.setImageResource(R.drawable.ic_new_notification)
        }

        val dialog = android.app.AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        closeButton.setOnClickListener { dialog.dismiss() }

        dialog.show()

        onClick(notif)
    }



    override fun getItemCount() = notifications.size
}

