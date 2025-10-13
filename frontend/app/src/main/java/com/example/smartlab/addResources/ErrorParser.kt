package com.example.smartlab.addResources

import com.example.smartlab.model.ErrorResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import okhttp3.ResponseBody

object ErrorParser {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(
            LocalDateTime::class.java,
            com.google.gson.JsonDeserializer { json, _, _ ->
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                LocalDateTime.parse(json.asString, formatter)
            })
        .create()

    /**
     * Перетворює ResponseBody в ErrorResponse.
     * Якщо парсинг не вдається, повертає ErrorResponse з мінімальними даними.
     */
    fun parse(responseBody: ResponseBody?): ErrorResponse {
        return try {
            responseBody?.string()?.let {
                gson.fromJson(it, ErrorResponse::class.java)
            } ?: ErrorResponse(
                timestamp = null,
                status = 0,
                error = "Unknown",
                message = "Empty error body",
                path = ""
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ErrorResponse(
                timestamp = null,
                status = 0,
                error = "ParseError",
                message = e.localizedMessage ?: "Unknown parse error",
                path = ""
            )
        }
    }
}