package com.example.smartlab

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartlab.addResources.NotificationManagerMy
import com.example.smartlab.service.GlobalNotificationManager
import com.example.smartlab.utils.PermissionHelper


open class BaseActivity : AppCompatActivity() {

    private var notificationBadge: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Встановлюємо поточну активність в GlobalNotificationManager
        GlobalNotificationManager.setCurrentActivity(this)
        android.util.Log.d("BaseActivity", "onCreate: ${javaClass.simpleName}")
        
        // Перевіряємо дозвіл на показ поверх інших додатків
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionHelper.hasSystemAlertWindowPermission(this)) {
                PermissionHelper.openSystemAlertWindowSettings(this)
            }
        }
    }

    fun initNav() {

        notificationBadge = findViewById<TextView>(R.id.notificationBadge)

        updateNotificationBadge()

    }


    fun updateNotificationBadge() {
        notificationBadge?.let { badge ->
            val count = NotificationManagerMy.unreadCount
            if (count > 0) {
                badge.visibility = View.VISIBLE
            } else {
                badge.visibility = View.GONE
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Встановлюємо поточну активність при поверненні
        GlobalNotificationManager.setCurrentActivity(this)
        android.util.Log.d("BaseActivity", "onResume: ${javaClass.simpleName}")
    }
    
    override fun onStart() {
        super.onStart()
        // Також встановлюємо при старті (для навігації назад)
        GlobalNotificationManager.setCurrentActivity(this)
        android.util.Log.d("BaseActivity", "onStart: ${javaClass.simpleName}")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Очищаємо поточну активність при закритті
        GlobalNotificationManager.setCurrentActivity(null)
        android.util.Log.d("BaseActivity", "onDestroy: ${javaClass.simpleName}")
    }
}
