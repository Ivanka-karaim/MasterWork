package com.example.smartlab.api
import com.example.smartlab.model.SignInModel
import com.example.smartlab.model.SignUpModel
import com.example.smartlab.dto.UserToken
import com.example.smartlab.model.ApiResponse
import com.example.smartlab.model.EditProfileModel
import com.example.smartlab.model.UserProfile
import okhttp3.ResponseBody

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApi {

    @GET("/api/profile")
    suspend fun profile(@Header("Authorization") token: String):retrofit2.Response<ApiResponse<UserProfile>>

    @PUT("/api/profile")
    suspend fun editProfile(@Header("Authorization") token: String, @Body editProfileModel: EditProfileModel):retrofit2.Response<ApiResponse<UserProfile>>

    @POST("/api/auth/signup")
    suspend fun signUp(@Body signUpModel: SignUpModel): retrofit2.Response<ApiResponse<Any>>

    @POST("/api/logout")
    suspend fun logout(@Header("Authorization") token: String): retrofit2.Response<ResponseBody>

    @POST("/api/auth/signin")
    suspend fun signIn(@Body signInModel: SignInModel): retrofit2.Response<ApiResponse<UserToken>>

}