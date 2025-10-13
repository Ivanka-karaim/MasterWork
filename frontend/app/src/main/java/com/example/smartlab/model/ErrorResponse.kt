package com.example.smartlab.model

data class ErrorResponse(
    val timestamp: String?,
    val status: Int,
    val error: String,
    val message: Any?,
    val path: String
)
