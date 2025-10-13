package com.example.smartlab.service
import android.app.Activity


import android.content.Context

import android.graphics.PixelFormat

import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import com.example.smartlab.R
import com.example.smartlab.addResources.ErrorHandler
import com.example.smartlab.addResources.NotificationManagerMy
import com.example.smartlab.addResources.RetrofitClient

import com.example.smartlab.api.NotificationApi
import com.example.smartlab.model.NotificationData
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener


class NotificationService(private val context: Context, private val userId: String) {

    private val api = RetrofitClient.getInstance().create(NotificationApi::class.java)

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    fun connect(onNotificationReceived: (NotificationData) -> Unit) {
        val request = Request.Builder()
            .url("ws://192.168.31.73:8082/ws/notifications?userId=$userId")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val notif = Gson().fromJson(text, NotificationData::class.java)
                onNotificationReceived(notif)
                NotificationManagerMy.unreadCount += 1
            }
        })
    }

    suspend fun getAllNotifications(token: String): List<NotificationData> {
        var notifications: List<NotificationData> = emptyList()
        val response = api.getUserNotifications(token)
        if (response.isSuccessful && response.body() != null) {
            notifications = response.body()!!.data
            NotificationManagerMy.unreadCount = notifications.count { !it.read }
        }else if (response.code() == 401 ){
            if(context is Activity) {
                ErrorHandler.unauthorizedUser(context)
            }
        }
        return notifications
    }


    suspend fun markNotificationAsRead(id: String, token: String) {
        val response = api.markAsRead(id, token)
        if(response.isSuccessful) {
            return
        } else if ( response.code() == 401 ){
            if(context is Activity) {
                ErrorHandler.unauthorizedUser(context)
            }
        } else{
            ErrorHandler.generalError(context)
        }
    }


    private fun showFloatingNotification(activity: Activity, title: String, message: String) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.view_notification_banner, null)

        val titleView = view.findViewById<TextView>(R.id.notificationTitle)
        val messageView = view.findViewById<TextView>(R.id.notificationMessage)
        titleView.text = title
        messageView.text = message

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP

        val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        activity.runOnUiThread {
            try {
                windowManager.addView(view, params)
                view.animate().alpha(1f).setDuration(300).start()
                Handler(Looper.getMainLooper()).postDelayed({
                    view.animate().alpha(0f).setDuration(300).withEndAction {
                        try { windowManager.removeView(view) } catch (_: Exception) {}
                    }.start()
                }, 3000)
            } catch (_: Exception) {}
        }
    }
}
