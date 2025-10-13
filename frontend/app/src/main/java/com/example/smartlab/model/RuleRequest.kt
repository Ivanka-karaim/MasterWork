package com.example.smartlab.model

import java.time.LocalDateTime

data class RuleRequest(
    val ruleType: String,          // "SENSOR" або "TIME"
    val deviceId: String?,           // сенсор
    val operator: String?,         // ">", "<", "="
    val threshold: Double?,        // порогове значення
    val actionDeviceId: String?,     // пристрій для керування
    val actionType: String?,       // "on", "off"
    val actionValue: Double?,      // 24
    val triggerTime: String?, // для TIME правил
    val active: Boolean,
    val mirrorRule: Boolean
)