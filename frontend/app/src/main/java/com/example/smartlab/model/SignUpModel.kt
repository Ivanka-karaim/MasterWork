package com.example.smartlab.model

data class SignUpModel(
    val fullName: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val role: String
)