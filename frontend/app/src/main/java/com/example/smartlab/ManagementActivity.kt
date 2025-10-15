package com.example.smartlab

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.*
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Visibility
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
            profileButton.setOnClickListener {
                startActivity(
                    Intent(
                        this@ManagementActivity,
                        ProfileActivity::class.java
                    )
                )
            }
            backButton.setOnClickListener { finish() }
            deviceSwitch.setOnCheckedChangeListener { _, isChecked ->
                onDeviceSwitchChanged(
                    isChecked
                )
            }
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
                } else if (response.code() == 401) {
                    ErrorHandler.unauthorizedUser(this@ManagementActivity)
                } else showToast("Помилка завантаження даних")
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
            when (val errors =
                ManagementService.createNewAction(api, deviceId, action, 0.0, "Bearer $token")) {
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
                val deviceInfo = api.getDeviceInfo(deviceId, "Bearer $token").body()?.data

                if (deviceInfo != null) {
                    binding.title.text = deviceInfo.title
                    setDeviceImage(deviceInfo.image)
                    if (deviceInfo.type == "ENERGY") {
                        binding.visibleEnergyCharge.visibility = View.VISIBLE
                        val response = api.getMeasurements(deviceId, "Bearer $token")
                        if (response.isSuccessful && response.body() != null) {
                            val measurements = api.getMeasurements(deviceId, "Bearer $token")
                                .body()?.data.orEmpty()
                            val level =
                                measurements.firstOrNull { it.parameterName == "LEVEL" }?.value
                            binding.energyCharge.text =
                                level?.let { "Рівень заряду: $it%" } ?: "Рівень заряду недоступний"

                            binding.statistics.setOnClickListener {
                                val intent =
                                    Intent(this@ManagementActivity, SensorDataActivity::class.java)
                                intent.putExtra("DEVICE_ID", deviceId)
                                this@ManagementActivity.startActivity(intent)
                            }
                            setupAutoModeButton(deviceInfo, api, token)


                        }


                    }
                }




                api.getDeviceLog(deviceId, "Bearer $token").body()?.data?.let {
                    binding.logRecyclerView.apply {
                        layoutManager = LinearLayoutManager(this@ManagementActivity)
                        adapter = EventLogAdapter(it)
                    }
                }
            } catch (e: Exception) {
                println(e)
                showToast("Не вдалося завантажити журнал: ${e.localizedMessage}")
            }
        }
    }

    fun setupAutoModeButton(deviceInfo: DeviceResponse, api: DeviceApi, token: String) {
        fun updateButton(mode: String) {
            if (mode == "AUTO") {
                binding.onAutoMode.background = ContextCompat.getDrawable(
                    this@ManagementActivity,
                    R.drawable.rounded_box_event_log
                )
                binding.onAutoMode.text = "Вимкнути режим АВТО"
            } else {
                binding.onAutoMode.background = ContextCompat.getDrawable(
                    this@ManagementActivity,
                    R.drawable.button
                )
                binding.onAutoMode.text = "Увімкнути режим АВТО"
            }
        }

        // спочатку встановлюємо правильний стан кнопки
        updateButton(deviceInfo.mode)

        binding.onAutoMode.setOnClickListener {
            // відразу оновлюємо UI
            val newMode = if (deviceInfo.mode == "AUTO") "NO_AUTO" else "AUTO"
            deviceInfo.mode = newMode // оновлюємо локально
            updateButton(newMode)

            // асинхронно повідомляємо сервер
            lifecycleScope.launch {
                try {
                    val response =
                        api.editDeviceMode(deviceId, ModeRequest(newMode), "Bearer $token")
                    when {
                        response.code() == 401 -> ErrorHandler.unauthorizedUser(this@ManagementActivity)
                        !response.isSuccessful -> ErrorHandler.generalError(this@ManagementActivity)
                    }
                } catch (e: Exception) {
                    showToast("Не вдалося змінити режим: ${e.localizedMessage}")
                    // якщо сервер не відповів, можна відкотити локальний стан
                    val revertMode = if (newMode == "AUTO") "NO_AUTO" else "AUTO"
                    deviceInfo.mode = revertMode
                    updateButton(revertMode)
                }
            }
        }
    }


    private fun setDeviceImage(imageBase64: String) {
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
