package com.example.smartlab.model

data class DeviceResponse(
    val id: String,
    val title: String,
    val description: String,
    val inventoryNumber: String,
    val image: String,
    val type: String,
    val on: Boolean
)
