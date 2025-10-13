package com.example.smartlab

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartlab.addResources.ErrorHandler
import com.example.smartlab.addResources.RetrofitClient
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.api.DeviceApi
import com.example.smartlab.databinding.ActivityRuleFilterBinding
import com.example.smartlab.databinding.ItemDeviceCheckboxBinding
import com.example.smartlab.model.DeviceResponse
import kotlinx.coroutines.launch

class FilterRuleActivity: AppCompatActivity() {
    private lateinit var binding: ActivityRuleFilterBinding
    private val selectedRuleTypes = mutableSetOf<String>()
    private val selectedDeviceIds = mutableSetOf<String>()
    private val selectedActionDeviceIds = mutableSetOf<String>()
    private var active: Boolean? = false
    private val deviceApi = RetrofitClient.getInstance().create(DeviceApi::class.java)
    private lateinit var token: String


    // Дані для прикладу
    private val ruleTypesList = listOf("SENSOR", "TIME")
    private var devicesList: MutableList<DeviceResponse> = mutableListOf()
    private var actionDevicesList: MutableList<DeviceResponse> = mutableListOf()

    private lateinit var ruleTypeAdapter: RuleTypeAdapter
    private lateinit var deviceAdapter: DeviceFilterAdapter
    private lateinit var actionDeviceAdapter: DeviceFilterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRuleFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        token = SharedPreferencesFactory(this).getSharedPreferences("TOKEN").toString()




        // Відновлення фільтрів з Intent
        (intent.getSerializableExtra("filters") as? HashMap<String, Any>)?.let { map ->
            (map["ruleTypes"] as? ArrayList<String>)?.let { selectedRuleTypes.addAll(it) }
            (map["deviceIds"] as? ArrayList<String>)?.let { selectedDeviceIds.addAll(it) }
            (map["actionDeviceIds"] as? ArrayList<String>)?.let { selectedActionDeviceIds.addAll(it) }
            active = map["active"] as? Boolean ?: false
        }
        loadData()
        binding.backButton.setOnClickListener {
            finish()
        }

        setupRecyclerViews()
        setupAdapters()
        setupListeners()
        restoreSelections()
    }

    private fun loadData(){
        lifecycleScope.launch {
            try {
                val devicesResponse = deviceApi.findAll("Bearer $token")
                if (devicesResponse.isSuccessful) {
                    val allDevices = devicesResponse.body()?.data ?: emptyList()
                    devicesList = allDevices.filter { it.type == "SENSOR" }.toMutableList()
                    println(devicesList.size)
                    actionDevicesList = allDevices.filter { it.type != "SENSOR" }.toMutableList()
                    deviceAdapter.devicesUpdated(devicesList)
                    actionDeviceAdapter.devicesUpdated(actionDevicesList)

                } else if(devicesResponse.code() == 401) {
                    ErrorHandler.unauthorizedUser(this@FilterRuleActivity)
                }
                else {
                    ErrorHandler.generalError(this@FilterRuleActivity)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ErrorHandler.generalError(this@FilterRuleActivity)
            }
        }
    }

    private fun setupRecyclerViews() {
        binding.ruleTypeRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.deviceRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.actionDeviceRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }



    private fun setupAdapters() {
        // Rule types
        ruleTypeAdapter = RuleTypeAdapter(ruleTypesList) { type ->
            if (!selectedRuleTypes.add(type)) selectedRuleTypes.remove(type)
        }
        binding.ruleTypeRecycler.adapter = ruleTypeAdapter

        // Devices
        deviceAdapter = DeviceFilterAdapter(mutableListOf()) { id, isChecked ->
            if (isChecked) selectedDeviceIds.add(id) else selectedDeviceIds.remove(id)
        }
        binding.deviceRecycler.adapter = deviceAdapter

        // Action devices
        actionDeviceAdapter = DeviceFilterAdapter(mutableListOf()) { id, isChecked ->
            if (isChecked) selectedActionDeviceIds.add(id) else selectedActionDeviceIds.remove(id)
        }
        binding.actionDeviceRecycler.adapter = actionDeviceAdapter
    }

    private fun setupListeners() {
        binding.activeCheckbox.isChecked = (active ?: false)
        binding.activeCheckbox.setOnCheckedChangeListener { _, isChecked ->
            active = isChecked
        }

        binding.applyFiltersButton.setOnClickListener {
            val filters = hashMapOf<String, Any>(
                "ruleTypes" to ArrayList(selectedRuleTypes),
                "deviceIds" to ArrayList(selectedDeviceIds),
                "actionDeviceIds" to ArrayList(selectedActionDeviceIds),
                "active" to (active ?: false)
            )
            val intent = Intent(this, RulesActivity::class.java)
            intent.putExtra("filters", filters)
            startActivity(intent)
        }
    }


    private fun restoreSelections() {
        ruleTypeAdapter.selectedTypes.addAll(selectedRuleTypes)
        ruleTypeAdapter.notifyDataSetChanged()

        deviceAdapter.selectedIds.addAll(selectedDeviceIds)
        deviceAdapter.notifyDataSetChanged()

        actionDeviceAdapter.selectedIds.addAll(selectedActionDeviceIds)
        actionDeviceAdapter.notifyDataSetChanged()
    }


    class RuleTypeAdapter(
        private val types: List<String>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<RuleTypeAdapter.TypeViewHolder>() {

        val selectedTypes = mutableSetOf<String>()

        inner class TypeViewHolder(val button: Button) : RecyclerView.ViewHolder(button)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TypeViewHolder {
            val btn = Button(parent.context)
            val params = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 8, 8, 8)
            btn.layoutParams = params
            return TypeViewHolder(btn)
        }

        override fun onBindViewHolder(holder: TypeViewHolder, position: Int) {
            val type = types[position]
            holder.button.text = type

            // Функція для створення drawable з кольором, бордером і заокругленням
            fun createBackground(selected: Boolean): GradientDrawable {
                return GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 30f // заокруглення
                    setColor(if (selected) 0xFF2196F3.toInt() else 0xFFFFFFFF.toInt()) // синій або білий
                    setStroke(4, 0xFF2196F3.toInt()) // товщина бордеру + колір
                }
            }

            // Встановлюємо фон залежно від стану
            holder.button.background = createBackground(selectedTypes.contains(type))

            holder.button.setOnClickListener {
                if (!selectedTypes.add(type)) selectedTypes.remove(type)
                // Оновлюємо фон після кліку
                holder.button.background = createBackground(selectedTypes.contains(type))
                onClick(type)
            }
        }


        override fun getItemCount(): Int = types.size
    }

    class DeviceFilterAdapter(
        private val devices: MutableList<DeviceResponse>,
        private val onSelectionChanged: (String, Boolean) -> Unit
    ) : RecyclerView.Adapter<DeviceFilterAdapter.DeviceViewHolder>() {

        val selectedIds = mutableSetOf<String>()

        inner class DeviceViewHolder(val binding: ItemDeviceCheckboxBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
            val binding = ItemDeviceCheckboxBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return DeviceViewHolder(binding)
        }

        override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
            val device = devices[position]
            holder.binding.checkboxName.text = device.title
            // Відключаємо старий listener
            holder.binding.checkbox.setOnCheckedChangeListener(null)

            // Встановлюємо правильний стан
            holder.binding.checkbox.isChecked = selectedIds.contains(device.id)

            // Підключаємо listener заново
            holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedIds.add(device.id) else selectedIds.remove(device.id)
                onSelectionChanged(device.id, isChecked)
            }
        }
        fun devicesUpdated(newDevices: List<DeviceResponse>) {
            devices.clear()
            devices.addAll(newDevices)
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = devices.size
    }






}