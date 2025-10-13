package com.example.smartlab.model

data class NotificationData(
    val id: String,
    val title: String,
    val message: String,
    var read: Boolean,
    val dateTime: String,
    val device: DeviceResponse
)
