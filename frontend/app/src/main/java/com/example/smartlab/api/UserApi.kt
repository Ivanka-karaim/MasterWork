package com.example.smartlab.api
import com.example.smartlab.model.SignInModel
import com.example.smartlab.model.SignUpModel
import com.example.smartlab.dto.UserToken
import com.example.smartlab.model.ApiResponse
import com.example.smartlab.model.EditProfileModel
import com.example.smartlab.model.RuleRequest
import com.example.smartlab.model.UserProfile
import okhttp3.ResponseBody

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.nio.file.attribute.UserPrincipal
import java.util.Objects

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


    @GET("/api/getAllUsers")
    suspend fun getAllUsers(@Header("Authorization") token: String): retrofit2.Response<ApiResponse<List<UserProfile>>>

    @PUT("/api/user/{id}/update")
    suspend fun updateUser(@Header("Authorization") token: String, @Path("id") id: String, @Body request: SignUpModel): retrofit2.Response<ApiResponse<UserProfile>>

    @DELETE("/api/user/{id}/delete")
    suspend fun deleteUser(@Header("Authorization") token: String, @Path("id") id: String): retrofit2.Response<ApiResponse<Objects>>

}