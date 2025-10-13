package com.example.smartlab.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper

import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartlab.R
import com.example.smartlab.addResources.RetrofitClient
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.api.ManagementApi
import com.example.smartlab.model.DeviceResponse
import com.example.smartlab.ManagementActivity
import com.example.smartlab.addResources.ErrorHandler
import com.example.smartlab.databinding.DialogHistoryRuleBinding
import com.example.smartlab.databinding.ItemHistoryRulesBinding
import com.example.smartlab.databinding.ItemRuleBinding
import com.example.smartlab.model.HistoryRuleResponse
import com.example.smartlab.model.RuleData
import com.example.smartlab.service.ManagementService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


class RuleAdapter(
    private val rules: MutableList<RuleData>,
    private val onEditClick: (RuleData) -> Unit,
    private val onDeleteClick: (RuleData) -> Unit
) :
    RecyclerView.Adapter<RuleAdapter.RuleViewHolder>() {

    inner class RuleViewHolder(val binding: ItemRuleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(rule: RuleData) = with(binding) {

            // Перевіряємо тип правила
            if (rule.ruleType.equals("SENSOR", ignoreCase = true)) {
                ruleSensor.visibility = View.VISIBLE
                timeSensor.visibility = View.GONE

                // Заповнюємо дані для сенсорного правила
                sensor.text = rule.sensor?.title ?: "—"
                condition.text = "${rule.operator ?: ""} ${rule.threshold ?: ""}"
                action.text = "${rule.action ?: ""} ${rule.actionDevice?.title ?: ""}"
                creatingUser.text = rule.historyRules?.lastOrNull()?.user?.fullName ?: "—"
                creatingDateTime.text = formatDateTime(rule.historyRules?.lastOrNull()?.dateTime)

                // Обробники натискань
                editRule.setOnClickListener { onEditClick(rule) }
                deleteRule.setOnClickListener { onDeleteClick(rule) }
                history.setOnClickListener { showHistoryDialog(rule) }

            } else if (rule.ruleType.equals("TIME", ignoreCase = true)) {
                ruleSensor.visibility = View.GONE
                timeSensor.visibility = View.VISIBLE

                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")

                // Заповнюємо дані для часового правила
                dateTimeForTime.text = formatDateTime(rule.triggerTime)
                actionForTime.text = "${rule.action ?: ""} ${rule.actionDevice?.title ?: ""}"
                creatingUserForTime.text = rule.historyRules?.lastOrNull()?.user?.fullName ?: "—"

                creatingDateTimeForTime.text = formatDateTime(rule.historyRules?.lastOrNull()?.dateTime)

                // Обробники натискань
                editRuleForTime.setOnClickListener { onEditClick(rule) }
                deleteRuleForTime.setOnClickListener { onDeleteClick(rule) }
                historyForTime.setOnClickListener { showHistoryDialog(rule) }
            }
        }


        fun formatDateTime(raw: String?): String {
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

        private fun showHistoryDialog(rule: RuleData) {
            val context = binding.root.context
            val dialogBinding = DialogHistoryRuleBinding.inflate(LayoutInflater.from(context))

            val dialog = AlertDialog.Builder(context)
                .setView(dialogBinding.root)
                .create()

            val recyclerView = dialogBinding.historyRulesRecyclerView
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = HistoryAdapter(rule.historyRules ?: emptyList())

            // кнопка закриття
            dialogBinding.closeButton.setOnClickListener { dialog.dismiss() }

            dialog.show()
        }
    }

    fun removeRule(rule: RuleData) {
        val index = rules.indexOfFirst { it.id == rule.id }
        if (index != -1) {
            rules.removeAt(index)
            notifyItemRemoved(index)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleViewHolder {
        val binding = ItemRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RuleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RuleViewHolder, position: Int) {
        holder.bind(rules[position])
    }

    override fun getItemCount(): Int = rules.size


    class HistoryAdapter(
        private val historyList: List<HistoryRuleResponse>
    ) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

        inner class HistoryViewHolder(private val binding: ItemHistoryRulesBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(history: HistoryRuleResponse) {
                val userName = history.user?.fullName ?: "Невідомий користувач"
                val date = formatDateTimeMy(history.dateTime.toString())
                binding.userAndDateForHistory.text = "$userName ($date)"
            }
            fun formatDateTimeMy(raw: String?): String {
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
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val binding =
                ItemHistoryRulesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return HistoryViewHolder(binding)
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            holder.bind(historyList[position])
        }

        override fun getItemCount(): Int = historyList.size
    }



}
