package com.example.smartlab.api
import retrofit2.Response
import com.example.smartlab.dto.ActionRequest
import com.example.smartlab.model.ApiResponse
import com.example.smartlab.model.Measurement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ManagementApi {

    @POST("/api/management")
    suspend fun createNewAction(@Header("Authorization") token: String, @Body actionRequest: ActionRequest): Response<ApiResponse<Boolean>>
    @GET("api/management/sensors/{deviceId}")
    suspend fun getMeasurements(
        @Header("Authorization") token: String,
        @Path("deviceId") deviceId: String?,
        @Query("from") from: String?,
        @Query("to") to: String?,
        @Query("minValue") min: Double?,
        @Query("maxValue") max: Double?
    ): Response<ApiResponse<List<Measurement>>>
}
