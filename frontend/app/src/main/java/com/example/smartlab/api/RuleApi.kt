package com.example.smartlab.api

import com.example.smartlab.model.ApiResponse
import com.example.smartlab.model.NotificationData
import com.example.smartlab.model.RuleData
import com.example.smartlab.model.RuleRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.Objects

interface RuleApi {
    @GET("api/rules")
    suspend fun getRules(@Header("Authorization") token: String,
                         @Query("ruleTypes") ruleTypes: List<String>?,
                         @Query("deviceIds") deviceIds: List<String>?,
                         @Query("actionDeviceIds") actionDeviceIds: List<String>?,
                         @Query("active") active: Boolean?): Response<ApiResponse<List<RuleData>>>
    @POST("api/rules")
    suspend fun createRule(@Header("Authorization") token: String, @Body request: RuleRequest ): Response<ApiResponse<RuleData>>
    @PUT("api/rules/{id}")
    suspend fun updateRule(@Header("Authorization") token: String, @Path("id") id: String,@Body request: RuleRequest): Response<ApiResponse<RuleData>>

    @DELETE("api/rules/{id}")
    suspend fun deleteRule(@Header("Authorization") token: String,@Path("id") id: String): Response<ApiResponse<Objects>>

}