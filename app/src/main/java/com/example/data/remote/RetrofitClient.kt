package com.example.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private var currentRetrofit: Retrofit? = null
    private var currentBaseUrl: String = ""

    fun getApiService(baseUrl: String): SmsGatewayApiService {
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        if (currentRetrofit == null || currentBaseUrl != normalizedUrl) {
            currentBaseUrl = normalizedUrl
            currentRetrofit = Retrofit.Builder()
                .baseUrl(normalizedUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        }
        return currentRetrofit!!.create(SmsGatewayApiService::class.java)
    }
}
