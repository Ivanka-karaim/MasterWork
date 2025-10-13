package com.example.smartlab

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartlab.adapter.DeviceAdapter
import com.example.smartlab.addResources.ErrorHandler
import com.example.smartlab.addResources.RetrofitClient
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.api.DeviceApi
import com.example.smartlab.databinding.ActivityDevicesBinding
import com.example.smartlab.service.GlobalNotificationManager

import kotlinx.coroutines.launch

class DevicesActivity:  BaseActivity()   {

    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var deviceApi: DeviceApi

    private lateinit var binding: ActivityDevicesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevicesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNav()

        navigation()

        binding.deviceRecyclerView.layoutManager = LinearLayoutManager(this)
        val token = SharedPreferencesFactory(this).getSharedPreferences("TOKEN")!!
        deviceApi = RetrofitClient.getInstance().create(DeviceApi::class.java)

        val type = intent.getStringExtra("TYPE") ?: return
        when (type) {
            "LIGHTING" -> binding.title.text = "Освітлення"
            "CLIMATE" -> binding.title.text = "Клімат"
            "MEASUREMENT" -> binding.title.text = "Датчики"

            else -> binding.title.text = ""
        }

        lifecycleScope.launch {
            val response = deviceApi.getDevicesByType(type, "Bearer $token")
            if (response.isSuccessful) {
                val devices = response.body()?.data ?: emptyList()
                deviceAdapter = DeviceAdapter(devices, this@DevicesActivity, lifecycleScope, binding.errorPopup, type)
                binding.deviceRecyclerView.adapter = deviceAdapter
            } else if (response.code() == 401) {
                ErrorHandler.unauthorizedUser(this@DevicesActivity)
            }
        }


    }
    private fun navigation(){
        binding.topToolbar.profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.bottomNav.home.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        binding.bottomNav.edit.setOnClickListener {
            Toast.makeText(this, "Create rules", Toast.LENGTH_SHORT).show()
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