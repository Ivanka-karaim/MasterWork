package com.example.smartlab

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.databinding.ActivityHomeBinding
import com.example.smartlab.service.GlobalNotificationManager


class HomeActivity:  BaseActivity()  {
    private lateinit var binding: ActivityHomeBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNav()
        navigation()

        val userId = SharedPreferencesFactory(this).getSharedPreferences("USER_ID") ?: ""
        if (userId.isNotEmpty()) {
            GlobalNotificationManager.setNotificationCallback { 
                updateNotificationBadge()
            }
        }

        binding.lamp.setOnClickListener{
            val intent = Intent(this, DevicesActivity::class.java)
            intent.putExtra("TYPE", "LIGHTING")
            startActivity(intent)

        }
        binding.climate.setOnClickListener{
            val intent = Intent(this, DevicesActivity::class.java)
            intent.putExtra("TYPE", "CLIMATE")
            startActivity(intent)

        }
        binding.sensors.setOnClickListener{
            val intent = Intent(this, SensorsActivity::class.java)
            startActivity(intent)
        }
        binding.energy.setOnClickListener {
            val intent = Intent(this, DevicesActivity::class.java)
            intent.putExtra("TYPE", "ENERGY")
            startActivity(intent)
        }

    }

    private fun navigation(){
        binding.topToolbar.profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.bottomNav.edit.setOnClickListener {
            startActivity(Intent(this, RulesActivity::class.java))
        }
        binding.bottomNav.users.setOnClickListener {
            Toast.makeText(this, "Users?students", Toast.LENGTH_SHORT).show()
        }
        binding.bottomNav.notificationFrame.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))

        }
    }

    override fun onResume() {
        super.onResume()
        updateNotificationBadge()
        GlobalNotificationManager.setCurrentActivity(this)
    }


}