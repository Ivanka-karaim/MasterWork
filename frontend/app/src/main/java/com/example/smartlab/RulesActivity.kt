package com.example.smartlab

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartlab.adapter.RuleAdapter
import com.example.smartlab.addResources.ErrorHandler
import com.example.smartlab.addResources.RetrofitClient
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.api.RuleApi
import com.example.smartlab.databinding.ActivityRulesBinding
import com.example.smartlab.model.RuleData
import kotlinx.coroutines.launch

class RulesActivity : BaseActivity() {
    private lateinit var adapter: RuleAdapter
    private lateinit var binding: ActivityRulesBinding
    private lateinit var ruleApi: RuleApi


    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRulesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNav()


        binding.rulesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.createRuleButton.setOnClickListener {
            createRule()
        }
        binding.filterButton.setOnClickListener {
            val filters = intent.getSerializableExtra("filters") as? HashMap<String, Any>

            val intent = Intent(this, FilterRuleActivity::class.java)
            intent.putExtra("filters", filters)
            startActivity(intent)
        }

        ruleApi = RetrofitClient.getInstance().create(RuleApi::class.java)
        loadData()

        navigation()
    }
    private fun loadData(){
        val filters = intent.getSerializableExtra("filters") as? HashMap<String, Any>

        val ruleTypes = filters?.get("ruleTypes") as? List<String>
        val deviceIds = filters?.get("deviceIds") as? List<String>
        val actionDeviceIds = filters?.get("actionDeviceIds") as? List<String>
        val active = filters?.get("active") as? Boolean ?
        lifecycleScope.launch {
            val token = SharedPreferencesFactory(this@RulesActivity)
                .getSharedPreferences("TOKEN") ?: return@launch
            val response =
                ruleApi.getRules(token ="Bearer $token", ruleTypes = ruleTypes,
                    deviceIds = deviceIds,
                    actionDeviceIds = actionDeviceIds,
                    active = active)
            if (response.isSuccessful) {
                val rules = response.body()?.data ?: emptyList()
                adapter = RuleAdapter(
                    rules = rules.toMutableList(),
                    onEditClick = { rule -> editRule(rule) },
                    onDeleteClick = { rule -> deleteRule(rule) }
                )
                binding.rulesRecyclerView.adapter = adapter
            } else if (response.code() == 401) {
                ErrorHandler.unauthorizedUser(this@RulesActivity)
            }
        }
    }

    private fun createRule() {
        val intent = Intent(this, RuleEditActivity::class.java)
        startActivity(intent)
    }

    private fun editRule(rule: RuleData) {
        val intent = Intent(this, RuleEditActivity::class.java)
        intent.putExtra("rule", rule)
        startActivity(intent)
    }

    /** 🗑️ Видалення правила */
    private fun deleteRule(rule: RuleData) {
        val token = SharedPreferencesFactory(this@RulesActivity)
            .getSharedPreferences("TOKEN")
        lifecycleScope.launch {
            val response = ruleApi.deleteRule("Bearer $token", rule.id)
            if(response.isSuccessful){
                adapter.removeRule(rule)
            } else if(response.code() == 401){
                ErrorHandler.unauthorizedUser(this@RulesActivity)
            } else {
                ErrorHandler.generalError(this@RulesActivity)
            }
        }

    }


    override fun onResume() {
        super.onResume()
        updateNotificationBadge()
        loadData()
    }

    private fun navigation() {
        binding.topToolbar.profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.bottomNav.home.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        binding.bottomNav.notification.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
        binding.bottomNav.users.setOnClickListener {
            Toast.makeText(this, "Users?students", Toast.LENGTH_SHORT).show()
        }
    }
}