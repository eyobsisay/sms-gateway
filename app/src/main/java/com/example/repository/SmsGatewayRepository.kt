package com.example.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.remote.RetrofitClient
import com.example.data.remote.DeviceRegistrationRequest
import com.example.data.remote.StatusUpdateRequest
import com.example.data.remote.BatchSyncRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class SmsGatewayRepository(context: Context) {

    private val dao = AppDatabase.getDatabase(context).smsGatewayDao()

    val deviceConfig: Flow<DeviceConfigEntity?> = dao.getDeviceConfig()
    val campaigns: Flow<List<CampaignEntity>> = dao.getAllCampaigns()
    val messages: Flow<List<SmsMessageEntity>> = dao.getAllMessages()
    val failedMessages: Flow<List<SmsMessageEntity>> = dao.getFailedMessages()
    val batches: Flow<List<SmsBatchEntity>> = dao.getAllBatches()
    val retryConfig: Flow<RetryConfigEntity?> = dao.getRetryConfig()
    val totalCount: Flow<Int> = dao.getTotalMessageCount()
    val sentCount: Flow<Int> = dao.getMessageCountByStatus("SENT")
    val deliveredCount: Flow<Int> = dao.getMessageCountByStatus("DELIVERED")
    val failedCount: Flow<Int> = dao.getMessageCountByStatus("SEND_FAILED")
    val queuedCount: Flow<Int> = dao.getMessageCountByStatus("QUEUED")

    val deviceConfigSync: DeviceConfigEntity?
        get() = try {
            // blocking call in getter or use suspend function
            null
        } catch (e: Exception) {
            null
        }

    suspend fun getDeviceConfigSyncSuspend(): DeviceConfigEntity? = withContext(Dispatchers.IO) {
        dao.getDeviceConfigSync()
    }

    suspend fun saveDeviceConfig(config: DeviceConfigEntity) = withContext(Dispatchers.IO) {
        dao.saveDeviceConfig(config)
    }

    suspend fun saveRetryConfig(config: RetryConfigEntity) = withContext(Dispatchers.IO) {
        dao.saveRetryConfig(config)
    }

    suspend fun insertMessage(message: SmsMessageEntity) = withContext(Dispatchers.IO) {
        dao.insertMessage(message)
    }

    suspend fun updateMessageStatus(messageId: String, status: String, failureReason: String? = null) = withContext(Dispatchers.IO) {
        dao.updateMessageStatus(messageId, status)
        val msg = dao.getMessageById(messageId)
        if (msg != null) {
            val eventId = UUID.randomUUID().toString()
            dao.insertSyncEvent(
                SyncEventEntity(
                    eventId = eventId,
                    messageId = messageId,
                    status = status,
                    timestamp = System.currentTimeMillis(),
                    synced = false
                )
            )
            dao.insertAttempt(
                SmsAttemptEntity(
                    attemptId = UUID.randomUUID().toString(),
                    messageId = messageId,
                    timestamp = System.currentTimeMillis(),
                    status = status,
                    failureReason = failureReason,
                    simSlot = msg.simUsed
                )
            )
        }
    }

    suspend fun getQueuedMessages(limit: Int): List<SmsMessageEntity> = withContext(Dispatchers.IO) {
        dao.getQueuedMessages(limit)
    }

    suspend fun getMessageById(messageId: String): SmsMessageEntity? = withContext(Dispatchers.IO) {
        dao.getMessageById(messageId)
    }

    suspend fun testConnectionAndRegister(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val config = dao.getDeviceConfigSync() ?: DeviceConfigEntity()
            if (config.apiToken.isBlank()) {
                return@withContext Result.failure(Exception("API Authentication Token is missing."))
            }
            val api = RetrofitClient.getApiService(config.serverBaseUrl)
            val request = DeviceRegistrationRequest(
                deviceIdentifier = config.deviceIdentifier,
                deviceName = config.deviceName,
                androidVersion = android.os.Build.VERSION.RELEASE,
                appVersion = "1.0"
            )
            val authHeader = if (config.apiToken.startsWith("Bearer ")) config.apiToken else "Bearer ${config.apiToken}"
            val response = api.registerDevice(authHeader, request)
            if (response.isSuccessful && response.body()?.success == true) {
                dao.saveDeviceConfig(config.copy(lastSuccessfulSync = System.currentTimeMillis(), lastHeartbeat = System.currentTimeMillis()))
                Result.success("Device successfully registered with remote gateway.")
            } else {
                Result.failure(Exception("Server returned error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAndStoreCampaignJobs(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val config = dao.getDeviceConfigSync() ?: return@withContext Result.failure(Exception("Configuration missing"))
            if (config.apiToken.isBlank()) {
                return@withContext Result.failure(Exception("API Token missing"))
            }
            val api = RetrofitClient.getApiService(config.serverBaseUrl)
            val authHeader = if (config.apiToken.startsWith("Bearer ")) config.apiToken else "Bearer ${config.apiToken}"
            val batchLimit = config.maxBatchSize.coerceIn(1, 50)
            val response = api.fetchCampaignJobs(authHeader, config.deviceIdentifier, batchLimit)
            if (response.isSuccessful && response.body()?.success == true) {
                val jobs = response.body()?.jobs ?: emptyList()
                var addedCount = 0
                for (job in jobs) {
                    val existing = dao.getMessageById(job.id)
                    if (existing == null) {
                        dao.insertMessage(
                            SmsMessageEntity(
                                id = job.id,
                                campaignId = job.campaignId,
                                recipientName = job.recipientName,
                                phoneNumber = job.phoneNumber,
                                body = job.body,
                                priority = job.priority,
                                scheduledTime = job.scheduledTime,
                                preferredSim = job.preferredSim,
                                status = "QUEUED"
                            )
                        )
                        dao.insertCampaign(
                            CampaignEntity(
                                campaignId = job.campaignId,
                                title = "Campaign ${job.campaignId}",
                                totalMessages = 1
                            )
                        )
                        addedCount++
                    }
                }
                Result.success(addedCount)
            } else {
                Result.failure(Exception("Failed to fetch campaign jobs: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncPendingEvents(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val config = dao.getDeviceConfigSync() ?: return@withContext Result.failure(Exception("Config missing"))
            if (config.apiToken.isBlank()) return@withContext Result.failure(Exception("Token missing"))
            val unsynced = dao.getUnsyncedEvents()
            if (unsynced.isEmpty()) return@withContext Result.success(0)

            val api = RetrofitClient.getApiService(config.serverBaseUrl)
            val authHeader = if (config.apiToken.startsWith("Bearer ")) config.apiToken else "Bearer ${config.apiToken}"
            
            val eventsReq = unsynced.map { ev ->
                val msg = dao.getMessageById(ev.messageId)
                StatusUpdateRequest(
                    messageId = ev.messageId,
                    campaignId = msg?.campaignId ?: "UNKNOWN",
                    deviceIdentifier = config.deviceIdentifier,
                    status = ev.status,
                    failureReason = msg?.failureReason,
                    timestamp = ev.timestamp,
                    simSlot = msg?.simUsed ?: -1
                )
            }

            val response = api.syncBatchEvents(authHeader, BatchSyncRequest(config.deviceIdentifier, eventsReq))
            if (response.isSuccessful) {
                for (ev in unsynced) {
                    dao.markEventSynced(ev.eventId)
                }
                dao.saveDeviceConfig(config.copy(lastSuccessfulSync = System.currentTimeMillis()))
                Result.success(unsynced.size)
            } else {
                Result.failure(Exception("Sync failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
