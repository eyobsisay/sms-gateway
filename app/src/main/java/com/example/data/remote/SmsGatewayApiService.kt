package com.example.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

data class DeviceRegistrationRequest(
    val deviceIdentifier: String,
    val deviceName: String,
    val androidVersion: String,
    val appVersion: String
)

data class DeviceRegistrationResponse(
    val success: Boolean,
    val message: String,
    val gatewayId: String?
)

data class CampaignJobDto(
    val id: String,
    val campaignId: String,
    val recipientName: String,
    val phoneNumber: String,
    val body: String,
    val priority: Int = 0,
    val scheduledTime: Long = 0L,
    val preferredSim: Int = -1
)

data class CampaignsResponse(
    val success: Boolean,
    val jobs: List<CampaignJobDto>
)

data class StatusUpdateRequest(
    val messageId: String,
    val campaignId: String,
    val deviceIdentifier: String,
    val status: String,
    val failureReason: String? = null,
    val timestamp: Long,
    val simSlot: Int
)

data class BatchSyncRequest(
    val deviceIdentifier: String,
    val events: List<StatusUpdateRequest>
)

data class GenericApiResponse(
    val success: Boolean,
    val message: String
)

interface SmsGatewayApiService {

    @POST("register")
    suspend fun registerDevice(
        @Header("Authorization") token: String,
        @Body request: DeviceRegistrationRequest
    ): Response<DeviceRegistrationResponse>

    @GET("jobs")
    suspend fun fetchCampaignJobs(
        @Header("Authorization") token: String,
        @Query("deviceIdentifier") deviceIdentifier: String
    ): Response<CampaignsResponse>

    @POST("status")
    suspend fun updateMessageStatus(
        @Header("Authorization") token: String,
        @Body request: StatusUpdateRequest
    ): Response<GenericApiResponse>

    @POST("batch-sync")
    suspend fun syncBatchEvents(
        @Header("Authorization") token: String,
        @Body request: BatchSyncRequest
    ): Response<GenericApiResponse>
}
