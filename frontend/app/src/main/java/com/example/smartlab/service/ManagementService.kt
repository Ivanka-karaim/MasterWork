package com.example.smartlab.service

import com.example.smartlab.addResources.ErrorParser
import com.example.smartlab.api.ManagementApi
import com.example.smartlab.api.UserApi
import com.example.smartlab.dto.ActionRequest
import com.example.smartlab.model.EditProfileModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ManagementService {
    suspend fun createNewAction(
        managementApi: ManagementApi,
        deviceId: String,
        action: String,
        value: Double,
        token: String
    ): String {
        return withContext(Dispatchers.IO) {
            val response = managementApi.createNewAction(
                token,
                ActionRequest(
                    deviceId, action, value
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

}