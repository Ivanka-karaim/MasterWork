package com.example.smartlab.api

import com.example.smartlab.model.ApiResponse
import com.example.smartlab.model.NotificationData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotificationApi {
    @GET("api/notifications")
    suspend fun getUserNotifications(@Header("Authorization") token: String): Response<ApiResponse<List<NotificationData>>>

    @PUT("api/notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String, @Header("Authorization") token: String): Response<ApiResponse<Object>>
}
