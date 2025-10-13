package com.example.smartlab

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartlab.adapter.SensorAdapter
import com.example.smartlab.addResources.ErrorHandler
import com.example.smartlab.addResources.RetrofitClient
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.api.DeviceApi
import com.example.smartlab.databinding.ActivityMeasurementsBinding
import kotlinx.coroutines.launch

class SensorsActivity: BaseActivity() {
    private lateinit var sensorAdapter: SensorAdapter
    private lateinit var deviceApi: DeviceApi
    private lateinit var binding: ActivityMeasurementsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeasurementsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNav()

        navigation()

        binding.sensorsRecyclerView.layoutManager = LinearLayoutManager(this)
        val token = SharedPreferencesFactory(this).getSharedPreferences("TOKEN")!!
        deviceApi = RetrofitClient.getInstance().create(DeviceApi::class.java)

        lifecycleScope.launch {
            val response = deviceApi.getDevicesByType("SENSOR", "Bearer $token")
            if (response.isSuccessful) {
                val devices = response.body()?.data ?: emptyList()
                sensorAdapter = SensorAdapter(devices, this@SensorsActivity)
                binding.sensorsRecyclerView.adapter = sensorAdapter
            } else if (response.code() == 401) {
                ErrorHandler.unauthorizedUser(this@SensorsActivity)
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
}