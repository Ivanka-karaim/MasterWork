package com.example.smartlab

import android.R
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartlab.addResources.ErrorHandler
import com.example.smartlab.addResources.RetrofitClient
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.api.DeviceApi
import com.example.smartlab.api.ManagementApi
import com.example.smartlab.api.RuleApi
import com.example.smartlab.databinding.ActivityNewRuleBinding
import com.example.smartlab.model.DeviceResponse
import com.example.smartlab.model.DeviceSimpleResponse
import com.example.smartlab.model.RuleData
import com.example.smartlab.model.RuleRequest
import kotlinx.coroutines.launch
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class RuleEditActivity: AppCompatActivity() {
    private lateinit var binding: ActivityNewRuleBinding
    private var currentRule: RuleData? = null // якщо != null → редагування

    private val ruleApi = RetrofitClient.getInstance().create(RuleApi::class.java)
    private val deviceApi = RetrofitClient.getInstance().create(DeviceApi::class.java)
    private lateinit var token: String

    private var deviceList: List<DeviceResponse> = emptyList()
    private var sensorsList: List<DeviceResponse> = emptyList()
    private val operatorList = listOf(">", "<", "=")
    private val actionList = listOf("ON", "OFF")

    private var selectedTriggerDateTimeMy: LocalDateTime? = null
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewRuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = SharedPreferencesFactory(this).getSharedPreferences("TOKEN").toString()

        currentRule = intent.getParcelableExtra("rule")

        setupUI()
        setupListeners()
        setupStaticSpinners()
        setupTimePicker()

        loadDevicesAndSensors {
            currentRule?.let { populateFields(it) }
        }
    }
    private fun setupTimePicker() = with(binding) {
        dateTimeForTime.setOnClickListener {
            val now = Calendar.getInstance()
            val year = now.get(Calendar.YEAR)
            val month = now.get(Calendar.MONTH)
            val day = now.get(Calendar.DAY_OF_MONTH)

            // Спочатку вибір дати
            DatePickerDialog(
                this@RuleEditActivity,
                { _, y, m, d ->
                    // Потім вибір часу
                    TimePickerDialog(
                        this@RuleEditActivity,
                        { _, hour, minute ->
                            selectedTriggerDateTimeMy= LocalDateTime.of(y, m + 1, d, hour, minute)
                            selectedTriggerDateTime.text = selectedTriggerDateTimeMy!!.format(dateTimeFormatter)
                        },
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                year, month, day
            ).show()
        }
    }


    private fun setupUI() = with(binding) {
        if (currentRule != null) {
            typeRule.isEnabled = false
            button.text = "Зберегти зміни"
            generalTitle.text = "Редагування правила"
        } else {
            button.text = "Створити правило"
            generalTitle.text = "Створення правила"
        }
    }

    private fun setupListeners() = with(binding) {
        backButton.setOnClickListener { finish() }

        typeRule.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val selected = parent?.getItemAtPosition(pos).toString()
                if (currentRule == null) {
                    if (selected == "SENSOR") {
                        sensorLinearLayout.visibility = View.VISIBLE
                        timeLinearLayout.visibility = View.GONE
                    } else if (selected == "TIME") {
                        sensorLinearLayout.visibility = View.GONE
                        timeLinearLayout.visibility = View.VISIBLE
                    } else{
                        sensorLinearLayout.visibility = View.GONE
                        timeLinearLayout.visibility = View.GONE
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        button.setOnClickListener {
            if (currentRule == null) saveNewRule() else updateRule()
        }
    }

    private fun setupStaticSpinners() = with(binding) {
        // Тип правила
        val types = listOf("","SENSOR", "TIME")
        typeRule.adapter = ArrayAdapter(
            this@RuleEditActivity,
            R.layout.simple_spinner_dropdown_item,
            types
        )

        // Оператори
        conditionOperatorSpinner.adapter = ArrayAdapter(
            this@RuleEditActivity,
            R.layout.simple_spinner_dropdown_item,
            operatorList
        )

        // Дії
        actionSpinner.adapter = ArrayAdapter(
            this@RuleEditActivity,
            R.layout.simple_spinner_dropdown_item,
            actionList
        )
        actionSpinnerForTime.adapter = ArrayAdapter(
            this@RuleEditActivity,
            R.layout.simple_spinner_dropdown_item,
            actionList
        )
    }

    private fun loadDevicesAndSensors(onLoaded: () -> Unit) {
        lifecycleScope.launch {
            try {
                val devicesResponse = deviceApi.findAll("Bearer $token")
                if (devicesResponse.isSuccessful) {
                    val allDevices = devicesResponse.body()?.data ?: emptyList()
                    deviceList = allDevices.filter { it.type != "SENSOR" }
                    sensorsList = allDevices.filter { it.type == "SENSOR" }

                    setupDeviceSpinners()
                    onLoaded()
                } else if(devicesResponse.code() == 401) {
                    ErrorHandler.unauthorizedUser(this@RuleEditActivity)
                }
                else {
                    ErrorHandler.generalError(this@RuleEditActivity)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ErrorHandler.generalError(this@RuleEditActivity)
            }
        }
    }

    private fun setupDeviceSpinners() = with(binding) {
        val deviceTitles = deviceList.map { it.title }
        val sensorTitles = sensorsList.map { it.title }

        val deviceAdapter = ArrayAdapter(
            this@RuleEditActivity,
            R.layout.simple_spinner_dropdown_item,
            deviceTitles
        )
        val sensorAdapter = ArrayAdapter(
            this@RuleEditActivity,
            R.layout.simple_spinner_dropdown_item,
            sensorTitles
        )

        deviceSpinner.adapter = deviceAdapter
        deviceSpinnerForTime.adapter = deviceAdapter
        sensorSpinner.adapter = sensorAdapter
    }

    private fun populateFields(rule: RuleData) = with(binding) {
        // Визначення типу
        if (rule.ruleType.equals("SENSOR", ignoreCase = true)) {
            sensorLinearLayout.visibility = View.VISIBLE
            timeLinearLayout.visibility = View.GONE
            typeRule.setSelection(1)

            // Заповнення
            val sensorIndex = sensorsList.indexOfFirst { it.id == rule.sensor?.id }
            if (sensorIndex != -1) sensorSpinner.setSelection(sensorIndex)

            val operatorIndex = operatorList.indexOf(rule.operator)
            if (operatorIndex != -1) conditionOperatorSpinner.setSelection(operatorIndex)

            val actionIndex = actionList.indexOf(rule.action)
            if (actionIndex != -1) actionSpinner.setSelection(actionIndex)

            val deviceIndex = deviceList.indexOfFirst { it.id == rule.actionDevice?.id }
            if (deviceIndex != -1) deviceSpinner.setSelection(deviceIndex)

            conditionValie.setText(rule.threshold?.toString() ?: "")
            isActive.isChecked = rule.active
        } else {
            timeLinearLayout.visibility = View.VISIBLE
            sensorLinearLayout.visibility = View.GONE
            typeRule.setSelection(2)

            val actionIndex = actionList.indexOf(rule.action)
            if (actionIndex != -1) actionSpinnerForTime.setSelection(actionIndex)

            val deviceIndex = deviceList.indexOfFirst { it.id == rule.actionDevice?.id }
            if (deviceIndex != -1) deviceSpinnerForTime.setSelection(deviceIndex)

            selectedTriggerDateTime.text = formatDateTime(rule.triggerTime)

            isActiveForTime.isChecked = rule.active
        }
    }
    private fun formatDateTime(raw: String?): String {
        if (raw.isNullOrBlank()) return "—"
        return try {
            val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")

            try {
                LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .format(outputFormatter)
            } catch (e: Exception) {
                OffsetDateTime.parse(raw).format(outputFormatter)
            }
        } catch (e: Exception) {
            "—"
        }
    }


    private fun saveNewRule() {
        val type = binding.typeRule.selectedItem.toString()
        if (type == "SENSOR") createSensorRule() else if(type == "TIME") createTimeRule()
    }

    private fun updateRule() {
        val type = currentRule?.ruleType ?: return
        if (type == "SENSOR") createSensorRule(true) else if(type == "TIME") createTimeRule(true)
    }

    private fun createSensorRule(isUpdate: Boolean = false) = with(binding) {
        val selectedSensor = sensorsList.getOrNull(sensorSpinner.selectedItemPosition)
        val selectedDevice = deviceList.getOrNull(deviceSpinner.selectedItemPosition)
        val operator = operatorList.getOrNull(conditionOperatorSpinner.selectedItemPosition)
        val action = actionList.getOrNull(actionSpinner.selectedItemPosition)
        val threshold = conditionValie.text.toString().toDoubleOrNull()
        val active = isActive.isChecked
        val mirrorRule = mirrorRule.isChecked

        lifecycleScope.launch {
            val request = RuleRequest(
                "SENSOR",
                selectedSensor?.id,
                operator,
                threshold,
                selectedDevice?.id,
                action, null,
                 null, active, false
            )

            val response = if (isUpdate && currentRule != null) {
                ruleApi.updateRule("Bearer $token", currentRule!!.id, request)
            } else {
                ruleApi.createRule("Bearer $token", request)
            }

            if (response.isSuccessful) finish()
            else if(response.code() == 401) ErrorHandler.unauthorizedUser(this@RuleEditActivity)
            else ErrorHandler.generalError(this@RuleEditActivity)
        }
    }

    private fun createTimeRule(isUpdate: Boolean = false) = with(binding) {
        val selectedDevice = deviceList.getOrNull(deviceSpinnerForTime.selectedItemPosition)
        val action = actionList.getOrNull(actionSpinnerForTime.selectedItemPosition)
        val active = isActiveForTime.isChecked
        val dateTime = selectedTriggerDateTime.text

        lifecycleScope.launch {
            val request = RuleRequest(
                 "TIME", null, null, null,
                 selectedDevice?.id,
                 action, null,
                 parseToIsoFormat(dateTime.toString()).toString(),
                 active, false
            )

            val response = if (isUpdate && currentRule != null) {
                ruleApi.updateRule("Bearer $token", currentRule!!.id, request)
            } else {
                ruleApi.createRule("Bearer $token", request)
            }

            if (response.isSuccessful) finish()
            else if(response.code() == 401) ErrorHandler.unauthorizedUser(this@RuleEditActivity)
            else ErrorHandler.generalError(this@RuleEditActivity)
        }
    }
    fun parseToIsoFormat(prettyDate: String?): String? {
        if (prettyDate.isNullOrBlank()) return null

        return try {
            val inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")
            val outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

            LocalDateTime.parse(prettyDate, inputFormatter).format(outputFormatter)
        } catch (e: Exception) {
            null
        }
    }




}