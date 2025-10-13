package com.example.smartlab.dto

data class UserToken(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String,
    val userId: String
)