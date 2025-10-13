package com.example.smartlab.addResources
import okhttp3.OkHttpClient

import okhttp3.logging.HttpLoggingInterceptor

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitClient {
    private var instance: Retrofit?=null

    fun getInstance(): Retrofit{
        if(instance==null){

            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            val trustManager: TrustManager = MyTrustManager()

            val sslContext: SSLContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(trustManager), null)

            val client = OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustManager as X509TrustManager)
                .hostnameVerifier { _, _ -> true }.addInterceptor(interceptor).build()

            instance = Retrofit.Builder()
                .baseUrl("http://192.168.31.73:8082/").client(client).addConverterFactory(
                    GsonConverterFactory.create()).build()
        }
        return instance!!
    }
}