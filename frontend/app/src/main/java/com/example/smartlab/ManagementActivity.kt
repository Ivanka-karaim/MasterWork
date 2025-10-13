package com.example.smartlab

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.*
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartlab.adapter.DeviceAdapter.DeviceViewHolder
import com.example.smartlab.adapter.EventLogAdapter
import com.example.smartlab.addResources.*
import com.example.smartlab.api.*
import com.example.smartlab.databinding.ActivityManagementBinding
import com.example.smartlab.model.*
import com.example.smartlab.service.ManagementService
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class ManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagementBinding
    private lateinit var deviceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deviceId = intent.getStringExtra("DEVICE_ID") ?: return

        with(binding) {
            dateRangePicker.setOnClickListener { showDateRangePicker() }
            profileButton.setOnClickListener { startActivity(Intent(this@ManagementActivity, ProfileActivity::class.java)) }
            backButton.setOnClickListener { finish() }
            deviceSwitch.setOnCheckedChangeListener { _, isChecked -> onDeviceSwitchChanged(isChecked) }
        }

        loadDeviceLog()
    }

    private fun showDateRangePicker() {
        val today = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            val start = Calendar.getInstance().apply { set(y, m, d) }
            Toast.makeText(this, "Тепер оберіть кінцеву дату", Toast.LENGTH_SHORT).show()

            DatePickerDialog(this, { _, ey, em, ed ->
                val end = Calendar.getInstance().apply { set(ey, em, ed) }
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val startText = sdf.format(start.time)
                val endText = sdf.format(end.time)
                binding.tvDateRange.text = "$startText - $endText"
                loadDeviceLogWithDateRange(startText, endText)
            }, today[Calendar.YEAR], today[Calendar.MONTH], today[Calendar.DAY_OF_MONTH]).apply {
                datePicker.minDate = start.timeInMillis
                setTitle("Виберіть кінцеву дату")
            }.show()
        }, today[Calendar.YEAR], today[Calendar.MONTH], today[Calendar.DAY_OF_MONTH])
            .apply { setTitle("Виберіть початкову дату") }
            .show()
    }

    private fun loadDeviceLogWithDateRange(from: String, to: String) {
        val api = RetrofitClient.getInstance().create(DeviceApi::class.java)
        val token = SharedPreferencesFactory(this).getSharedPreferences("TOKEN") ?: return

        lifecycleScope.launch {
            try {
                val response = api.getDeviceLogByDate(deviceId, from, to, "Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        binding.logRecyclerView.apply {
                            layoutManager = LinearLayoutManager(this@ManagementActivity)
                            adapter = EventLogAdapter(it)
                        }
                    }
                }else if (response.code() == 401){
                    ErrorHandler.unauthorizedUser(this@ManagementActivity)
                }else showToast("Помилка завантаження даних")
            } catch (e: Exception) {
                showToast("Не вдалося завантажити журнал")
            }
        }
    }

    private fun onDeviceSwitchChanged(isChecked: Boolean) {
        val api = RetrofitClient.getInstance().create(ManagementApi::class.java)
        val token = SharedPreferencesFactory(this).getSharedPreferences("TOKEN") ?: return
        val action = if (isChecked) "ON" else "OFF"

        lifecycleScope.launch {
            when (val errors = ManagementService.createNewAction(api, deviceId, action, 0.0, "Bearer $token")) {
                "" -> {}
                "unknownError" -> ErrorHandler.generalError(this@ManagementActivity)
                "unauthorized" -> ErrorHandler.unauthorizedUser(this@ManagementActivity)
                else -> showError(errors)
            }
        }
    }

    private fun loadDeviceLog() {
        val api = RetrofitClient.getInstance().create(DeviceApi::class.java)
        val token = SharedPreferencesFactory(this).getSharedPreferences("TOKEN") ?: return

        lifecycleScope.launch {
            try {
                api.getDeviceInfo(deviceId, "Bearer $token").body()?.data?.let {
                    binding.title.text = it.title
                    setDeviceImage(it.image)
                }

                api.getDeviceLog(deviceId, "Bearer $token").body()?.data?.let {
                    binding.logRecyclerView.apply {
                        layoutManager = LinearLayoutManager(this@ManagementActivity)
                        adapter = EventLogAdapter(it)
                    }
                }
            } catch (e: Exception) {
                showToast("Не вдалося завантажити журнал: ${e.localizedMessage}")
            }
        }
    }
    private fun setDeviceImage( imageBase64: String) {
        val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        binding.deviceImage.setImageBitmap(bitmap)
    }

    private fun showError(msg: String) {
        binding.errorPopup.apply {
            text = msg
            visibility = View.VISIBLE
        }
        Handler(Looper.getMainLooper()).postDelayed({
            binding.errorPopup.visibility = View.GONE
        }, 3000)
    }

    private fun showToast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
