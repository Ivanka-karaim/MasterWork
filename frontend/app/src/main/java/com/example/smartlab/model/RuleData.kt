package com.example.smartlab.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

import java.security.Timestamp
@Parcelize
data class RuleData(
    val id: String,
    val ruleType: String,
    val sensor: DeviceSimpleResponse?,
    val operator: String?,
    val threshold: Double?,
    val actionDevice: DeviceSimpleResponse?,
    val action: String?,
    val actionValue: Double?,
    val triggerTime: String?,
    val active: Boolean,
    val historyRules: List<HistoryRuleResponse>?
): Parcelable