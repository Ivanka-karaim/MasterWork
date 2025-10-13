package com.example.smartlab.service

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.smartlab.NotificationActivity
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

@SuppressLint("StaticFieldLeak")
object GlobalNotificationManager {

    private const val TAG = "GlobalNotificationManager"
    private const val WEBSOCKET_URL = "ws://192.168.31.73:8082/ws/notifications"

    private var context: Context? = null
    private var webSocket: WebSocket? = null
    private var client: OkHttpClient? = null
    private var isConnected = false
    private var currentUserId: String? = null
    private var currentActivity: Activity? = null
    private var notificationApi: NotificationApi? = null
    private val activeNotificationViews = mutableListOf<android.view.View>()
    private val activityStack = mutableListOf<Activity>()

    // Callback для сповіщень
    private var notificationCallback: ((NotificationData) -> Unit)? = null

    fun initialize(context: Context) {
        this.context = context.applicationContext
        createNotificationChannel(context)
        this.client = OkHttpClient()
        this.notificationApi = RetrofitClient.getInstance().create(NotificationApi::class.java)

        // Реєструємо ActivityLifecycleCallbacks для відстеження поточної активності
        if (context is Application) {
            context.registerActivityLifecycleCallbacks(object :
                Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    Log.d(TAG, "Activity created: ${activity.javaClass.simpleName}")
                }

                override fun onActivityStarted(activity: Activity) {
                    if (!activityStack.contains(activity)) {
                        activityStack.add(activity)
                    }
                    updateCurrentActivity()
                    Log.d(TAG, "Activity started: ${activity.javaClass.simpleName}")
                }

                override fun onActivityResumed(activity: Activity) {
                    // Переміщуємо активність на вершину стеку
                    activityStack.remove(activity)
                    activityStack.add(activity)
                    updateCurrentActivity()
                    Log.d(TAG, "Activity resumed: ${activity.javaClass.simpleName}")
                }

                override fun onActivityPaused(activity: Activity) {
                    Log.d(TAG, "Activity paused: ${activity.javaClass.simpleName}")
                }

                override fun onActivityStopped(activity: Activity) {
                    Log.d(TAG, "Activity stopped: ${activity.javaClass.simpleName}")
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

                override fun onActivityDestroyed(activity: Activity) {
                    activityStack.remove(activity)
                    updateCurrentActivity()
                    Log.d(TAG, "Activity destroyed: ${activity.javaClass.simpleName}")
                }
            })
        }

        Log.d(TAG, "GlobalNotificationManager initialized")
    }

    fun connect(userId: String, callback: ((NotificationData) -> Unit)? = null) {
        if (isConnected && currentUserId == userId) {
            Log.d(TAG, "Already connected for user: $userId")
            return
        }

        // Закриваємо попереднє з'єднання якщо є
        disconnect()

        currentUserId = userId
        notificationCallback = callback

        val request = Request.Builder()
            .url("$WEBSOCKET_URL?userId=$userId")
            .build()

        webSocket = client?.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                isConnected = true
                Log.d(TAG, "WebSocket connected for user: $userId")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received message: $text")
                Log.d(TAG, "Current activity: ${currentActivity?.javaClass?.simpleName}")
                Log.d(TAG, "Activity stack size: ${activityStack.size}")

                try {
                    val notification = Gson().fromJson(text, NotificationData::class.java)

                    // Оновлюємо лічильник
                    NotificationManagerMy.unreadCount += 1

                    // Показуємо спливаюче сповіщення
//                    showFloatingNotification(notification)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.POST_NOTIFICATIONS
                            )
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                currentActivity ?: return,
                                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                                1001
                            )
                        }
                    }

                    showSystemNotification(notification)

                    // Викликаємо callback якщо є
                    notificationCallback?.invoke(notification)

                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing notification: ${e.message}")
                }
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: okhttp3.Response?
            ) {
                isConnected = false
                Log.e(TAG, "WebSocket connection failed: ${t.message}")

                // Автоматичне перепідключення через 5 секунд
                Handler(Looper.getMainLooper()).postDelayed({
                    if (currentUserId != null) {
                        Log.d(TAG, "Attempting to reconnect...")
                        connect(currentUserId!!, notificationCallback)
                    }
                }, 5000)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                Log.d(TAG, "WebSocket closing: $code - $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                Log.d(TAG, "WebSocket closed: $code - $reason")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Disconnecting")
        webSocket = null
        isConnected = false
        currentUserId = null
        Log.d(TAG, "WebSocket disconnected")
    }

    fun isConnected(): Boolean = isConnected

    fun setNotificationCallback(callback: (NotificationData) -> Unit) {
        this.notificationCallback = callback
    }

    fun setCurrentActivity(activity: Activity?) {
        // Очищаємо спливаючі сповіщення при зміні активності
        if (this.currentActivity != activity) {
            clearAllNotificationViews()
        }
        this.currentActivity = activity
        Log.d(TAG, "Current activity set to: ${activity?.javaClass?.simpleName}")
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "global_notifications"
            val channelName = "Global Notifications"
            val channelDescription = "App notifications"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel(channelId, channelName, importance)
            channel.description = channelDescription
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 500, 100, 500)
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(channel)
        }
    }


    private fun updateCurrentActivity() {
        // Отримуємо останню активність зі стеку
        val newCurrentActivity = activityStack.lastOrNull()

        if (currentActivity != newCurrentActivity) {
            Log.d(
                TAG,
                "Updating current activity from ${currentActivity?.javaClass?.simpleName} to ${newCurrentActivity?.javaClass?.simpleName}"
            )
            currentActivity = newCurrentActivity
        }

        Log.d(TAG, "Activity stack: ${activityStack.map { it.javaClass.simpleName }}")
    }

    private fun shortenText(text: String?, maxLength: Int): String {
        if (text.isNullOrEmpty()) return ""
        return if (text.length > maxLength) text.take(maxLength) + "…" else text
    }

    private fun showSystemNotification(notification: NotificationData) {
        val context = this.context ?: return
        val channelId = "global_notifications"

        val intent = Intent(context, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo) // Замінити на свій значок
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // звук, вібрація, світло

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(notification.id.hashCode(), builder.build())
    }


    private fun showFloatingNotification(notification: NotificationData) {
        var activityToUse = this.currentActivity

        // Якщо поточна активність не встановлена, спробуємо знайти її через стек
        if (activityToUse == null) {
            Log.w(TAG, "Current activity is null, trying to find from activity stack")
            activityToUse = activityStack.lastOrNull()
            Log.d(TAG, "Found activity from stack: ${activityToUse?.javaClass?.simpleName}")
        }

        // Перевіряємо чи активність ще жива
        if (activityToUse != null && (activityToUse.isFinishing || activityToUse.isDestroyed)) {
            Log.w(TAG, "Activity is finishing or destroyed, removing from stack")
            activityStack.remove(activityToUse)
            activityToUse = activityStack.lastOrNull()
        }

        if (activityToUse == null) {
            Log.w(TAG, "No current activity found, trying fallback notification")
            Log.d(TAG, "Activity stack: ${activityStack.map { it.javaClass.simpleName }}")

            // Резервний варіант - показуємо Toast
            showFallbackNotification(notification)
            return
        }

        Log.d(TAG, "Showing floating notification on: ${activityToUse.javaClass.simpleName}")
        Log.d(
            TAG,
            "Activity state: isFinishing=${activityToUse.isFinishing}, isDestroyed=${activityToUse.isDestroyed}"
        )

        try {
            val inflater = LayoutInflater.from(activityToUse)
            val view = inflater.inflate(R.layout.view_notification_banner, null)

            val titleView = view.findViewById<TextView>(R.id.notificationTitle)
            val messageView = view.findViewById<TextView>(R.id.notificationMessage)
            titleView.text = shortenText(notification.title, 35)
            messageView.text = shortenText(notification.message, 75)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
            params.width = (activityToUse.resources.displayMetrics.widthPixels * 0.9).toInt()
            params.gravity = Gravity.TOP
            params.y = 100

            val windowManager =
                activityToUse.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            activityToUse.runOnUiThread {
                try {
                    windowManager.addView(view, params)
                    activeNotificationViews.add(view)
                    view.alpha = 0f
                    view.animate().alpha(1f).setDuration(300).start()

                    Handler(Looper.getMainLooper()).postDelayed({
                        removeNotificationView(view, windowManager)
                    }, 4000)
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing floating notification: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating floating notification: ${e.message}")
        }
    }


    fun logout() {
        disconnect()
        clearAllNotificationViews()
        Log.d(TAG, "User logged out, disconnected WebSocket")
    }

    private fun removeNotificationView(view: android.view.View, windowManager: WindowManager) {
        view.animate().alpha(0f).setDuration(300).withEndAction {
            try {
                windowManager.removeView(view)
                activeNotificationViews.remove(view)
                Log.d(TAG, "Notification view removed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing notification view: ${e.message}")
            }
        }.start()
    }

    private fun clearAllNotificationViews() {
        Log.d(TAG, "Clearing all notification views: ${activeNotificationViews.size}")
        val currentActivity = this.currentActivity
        if (currentActivity != null) {
            val windowManager =
                currentActivity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val viewsToRemove = activeNotificationViews.toList() // Копіюємо список
            activeNotificationViews.clear()

            viewsToRemove.forEach { view ->
                try {
                    windowManager.removeView(view)
                    Log.d(TAG, "Removed notification view during cleanup")
                } catch (e: Exception) {
                    Log.e(TAG, "Error removing notification view during cleanup: ${e.message}")
                }
            }
        }
    }

    private fun getAllActivities(): String {
        // Повертаємо інформацію про поточну активність для діагностики
        return "Current activity: ${currentActivity?.javaClass?.simpleName ?: "null"}"
    }

    private fun showFallbackNotification(notification: NotificationData) {
        Log.d(TAG, "Showing fallback notification: ${notification.title}")

        // Показуємо Toast як резервний варіант
        val context = this.context
        if (context != null) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    "🔔 ${notification.title}: ${notification.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Методи для роботи з API сповіщень
    suspend fun getAllNotifications(token: String): List<NotificationData> {
        return try {
            val response = notificationApi?.getUserNotifications(token)
            if (response?.isSuccessful == true && response.body() != null) {
                val notifications = response.body()!!.data
                NotificationManagerMy.unreadCount = notifications.count { !it.read }
                notifications
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting notifications: ${e.message}")
            emptyList()
        }
    }

    suspend fun markNotificationAsRead(id: String, token: String): Boolean {
        return try {
            val response = notificationApi?.markAsRead(id, token)
            response?.isSuccessful == true
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as read: ${e.message}")
            false
        }
    }
}
