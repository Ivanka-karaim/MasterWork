package com.example.smartlab.api

import com.example.smartlab.model.ApiResponse
import com.example.smartlab.model.DeviceLog
import com.example.smartlab.model.DeviceResponse
import com.example.smartlab.model.Measurement
import com.example.smartlab.model.ModeRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DeviceApi {

    @GET("/api/devices/type/{type}")
    suspend fun getDevicesByType(@Path("type") type: String, @Header("Authorization") token: String): Response<ApiResponse<List<DeviceResponse>>>
    @GET("/api/devices")
    suspend fun findAll(@Header("Authorization") token: String): Response<ApiResponse<List<DeviceResponse>>>



    @GET("/api/devices/{id}")
    suspend fun getDeviceInfo(
        @Path("id") deviceId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<DeviceResponse>>
    @PUT("/api/devices/{id}/mode")
    suspend fun editDeviceMode(
        @Path("id") deviceId: String,
        @Body mode: ModeRequest,
        @Header("Authorization") token: String
    ): Response<ApiResponse<DeviceResponse>>

    @GET("/api/devices/{id}/measurements")
    suspend fun getMeasurements(
        @Path("id") deviceId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<Measurement>>>

    @GET("/api/devices/{id}/logs")
    suspend fun getDeviceLog(
        @Path("id") deviceId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<DeviceLog>>>

    @GET("/api/devices/{id}/logs")
    suspend fun getDeviceLogByDate(
        @Path("id") deviceId: String,
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<DeviceLog>>>


}