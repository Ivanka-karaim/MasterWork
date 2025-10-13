package com.example.smartlab.dto

data class ActionRequest(
    val deviceId: String,
    val action: String,
    val value: Double
)
