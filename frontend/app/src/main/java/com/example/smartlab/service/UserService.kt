package com.example.smartlab.service

import android.content.Context
import com.example.smartlab.addResources.ErrorParser
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.api.UserApi
import com.example.smartlab.dto.UserToken
import com.example.smartlab.model.ApiResponse
import com.example.smartlab.model.EditProfileModel
import com.example.smartlab.model.ErrorResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.smartlab.model.SignInModel
import com.example.smartlab.model.SignUpModel
import com.google.gson.Gson

object UserService {

    suspend fun signIn(
        userApi: UserApi,
        email: String,
        password: String,
        context: Context
    ): String {

        return withContext(Dispatchers.IO) {
            val response = userApi.signIn(
                SignInModel(
                    email, password
                )
            )
            if (response.isSuccessful) {
                val token = response.body()?.data?.accessToken
                if (token != null) {
                    SharedPreferencesFactory(context).saveSharedPreferences("TOKEN", token)
                }
                val userId = response.body()?.data?.userId
                if(userId != null) {
                    SharedPreferencesFactory(context).saveSharedPreferences("USER_ID", userId)

                    GlobalNotificationManager.connect(userId)
                }
                val role = response.body()?.data?.role
                if(role != null){
                    SharedPreferencesFactory(context).saveSharedPreferences("ROLE", role)
                }

            } else {
                val errorBody = response.errorBody()
                val errorResponse = ErrorParser.parse(errorBody)

                return@withContext when {
                    errorResponse.status == 500 -> "unknownError"
                    errorResponse.status == 401 -> "unauthorized"
                    else -> errorResponse.message.toString()
                }
            }
            return@withContext ""
        }
    }

    suspend fun editProfile(
        userApi: UserApi,
        fullName: String,
        email: String,
        token: String
    ): String {
        return withContext(Dispatchers.IO) {
            val response = userApi.editProfile(
                token,
                EditProfileModel(
                    fullName, email
                )
            )
            if (response.isSuccessful){

            }else{
                val errorBody = response.errorBody()
                val errorResponse = ErrorParser.parse(errorBody)
                return@withContext when {
                    errorResponse.status == 500 -> "unknownError"
                    errorResponse.status == 401 -> "unauthorized"
                    else -> errorResponse.message.toString()
                }
            }
            return@withContext ""
        }
    }

    suspend fun signUp(
        userApi: UserApi,
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        context: Context
    ): String {

        return withContext(Dispatchers.IO) {
            val response = userApi.signUp(
                SignUpModel(
                    fullName, email, password, confirmPassword, "STUDENT"
                )
            )
            if (response.isSuccessful) {
                return@withContext ""

            } else {
                val errorBody = response.errorBody()
                val errorResponse = ErrorParser.parse(errorBody)

                return@withContext when {
                    errorResponse.status == 500 -> "unknownError"
                    errorResponse.status == 401 -> "unauthorized"
                    else -> errorResponse.message.toString()
                }
            }
        }
    }
}