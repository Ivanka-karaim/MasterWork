package com.example.smartlab.model

import java.time.LocalDateTime

data class DeviceLog(
    val id: String,
    val dateTime: String,
    val actionValue: Double,
    val actionType: String,
    val deviceName: String,
    val userName: String
)