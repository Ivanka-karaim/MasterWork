package com.example.smartlab

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.smartlab.adapter.SensorPagerAdapter
import com.example.smartlab.addResources.ErrorHandler
import com.example.smartlab.addResources.MeasurementFilters
import com.example.smartlab.addResources.RetrofitClient
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.api.DeviceApi
import com.example.smartlab.api.ManagementApi
import com.example.smartlab.fragments.ChartFragment
import com.example.smartlab.fragments.TableFragment
import com.example.smartlab.model.Measurement
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class SensorDataActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private var currentMeasurements: List<Measurement> = emptyList()
    private lateinit var adapter: SensorPagerAdapter
    private var deviceId: String? = null
    private var filters: MeasurementFilters? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_data)

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        adapter = SensorPagerAdapter(this)
        viewPager.adapter = adapter

        deviceId = intent.getStringExtra("DEVICE_ID").toString()

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Графік" else "Таблиця"
        }.attach()

        val filterButton = findViewById<FloatingActionButton>(R.id.filterButton)
        filterButton.setOnClickListener {
            val intent = Intent(this, FilterActivity::class.java).putExtra("filters", filters)
            startActivityForResult(intent, FILTER_REQUEST_CODE)
        }
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        loadMeasurements(null)
    }

    private fun loadMeasurements(filters: MeasurementFilters?) {
        // тут робимо запит на бекенд через Retrofit
        lifecycleScope.launch {
            try {
                val token =
                    SharedPreferencesFactory(this@SensorDataActivity).getSharedPreferences("TOKEN")!!
                val managementApi = RetrofitClient.getInstance().create(ManagementApi::class.java)
                val deviceApi = RetrofitClient.getInstance().create(DeviceApi::class.java)

                val responseDevice = deviceId?.let { deviceApi.getDeviceInfo(it, "Bearer $token") }

                if (responseDevice!!.isSuccessful && responseDevice.body() != null) {
                    val device = responseDevice.body()!!.data
                    findViewById<TextView>(R.id.title).text = device.title

                }

                val response = managementApi
                    .getMeasurements(
                        "Bearer $token",
                        deviceId,
                        filters?.from,
                        filters?.to,
                        filters?.minValue,
                        filters?.maxValue
                    )
                if (response.isSuccessful && response.body() != null) {
                    currentMeasurements = response.body()!!.data
                } else if (response.code() == 401) {
                    ErrorHandler.unauthorizedUser(this@SensorDataActivity)
                } else {
                    ErrorHandler.generalError(this@SensorDataActivity)
                }

                adapter.updateData(currentMeasurements)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILTER_REQUEST_CODE && resultCode == RESULT_OK) {
            filters = data?.getParcelableExtra<MeasurementFilters>("filters")!!
            loadMeasurements(filters)
        }
    }

    companion object {
        const val FILTER_REQUEST_CODE = 1001
    }
}
