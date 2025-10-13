package com.example.smartlab.model

data class Measurement (
    val id: String,
    val deviceInventoryNumber: String,
    val parameterName: String,
    val value: Double,
    val dateTime: String
)