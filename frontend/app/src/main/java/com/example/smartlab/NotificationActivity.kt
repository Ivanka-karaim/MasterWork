package com.example.smartlab

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartlab.adapter.NotificationAdapter
import com.example.smartlab.addResources.NotificationManagerMy
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.databinding.ActivityNotificationBinding
import com.example.smartlab.service.GlobalNotificationManager
import com.example.smartlab.service.NotificationService
import kotlinx.coroutines.launch

class NotificationActivity : BaseActivity() {

    private lateinit var adapter: NotificationAdapter
    private lateinit var binding: ActivityNotificationBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNav()
        
        // Встановлюємо callback для оновлення UI при отриманні сповіщень
        GlobalNotificationManager.setNotificationCallback { notif ->
            runOnUiThread {
                adapter.addNotification(notif)
                updateNotificationBadge()
            }
        }

        binding.notificationsRecycler.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter { notif ->
            lifecycleScope.launch {
                val token = SharedPreferencesFactory(this@NotificationActivity)
                    .getSharedPreferences("TOKEN") ?: return@launch
                GlobalNotificationManager.markNotificationAsRead(notif.id, "Bearer $token")
                NotificationManagerMy.unreadCount -= 1
                loadNotifications("Bearer $token")
            }
        }
        binding.notificationsRecycler.adapter = adapter

        val token = SharedPreferencesFactory(this).getSharedPreferences("TOKEN") ?: return

        lifecycleScope.launch {
            loadNotifications("Bearer $token")

        }
        navigation()
    }


    private suspend fun loadNotifications(token: String) {
        val list = GlobalNotificationManager.getAllNotifications(token)
        adapter.setData(list)
    }

    override fun onResume() {
        super.onResume()
        updateNotificationBadge()
    }
    private fun navigation(){
        binding.topToolbar.profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.bottomNav.home.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        binding.bottomNav.edit.setOnClickListener {
            startActivity(Intent(this, RulesActivity::class.java))
        }
        binding.bottomNav.users.setOnClickListener {
            Toast.makeText(this, "Users?students", Toast.LENGTH_SHORT).show()
        }
    }
}
