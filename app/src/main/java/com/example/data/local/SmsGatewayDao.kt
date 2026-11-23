package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsGatewayDao {

    // Device Config
    @Query("SELECT * FROM device_config WHERE id = 1")
    fun getDeviceConfig(): Flow<DeviceConfigEntity?>

    @Query("SELECT * FROM device_config WHERE id = 1")
    suspend fun getDeviceConfigSync(): DeviceConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDeviceConfig(config: DeviceConfigEntity)

    // Campaigns
    @Query("SELECT * FROM campaigns ORDER BY createdAt DESC")
    fun getAllCampaigns(): Flow<List<CampaignEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: CampaignEntity)

    // SMS Messages
    @Query("SELECT * FROM sms_messages ORDER BY scheduledTime DESC")
    fun getAllMessages(): Flow<List<SmsMessageEntity>>

    @Query("SELECT * FROM sms_messages WHERE status = :status ORDER BY priority DESC")
    fun getMessagesByStatus(status: String): Flow<List<SmsMessageEntity>>

    @Query("SELECT * FROM sms_messages WHERE status = 'QUEUED' ORDER BY priority DESC LIMIT :limit")
    suspend fun getQueuedMessages(limit: Int): List<SmsMessageEntity>

    @Query("SELECT * FROM sms_messages WHERE status IN ('SEND_FAILED', 'DELIVERY_FAILED', 'DELIVERY_UNKNOWN') ORDER BY lastAttemptTimestamp DESC")
    fun getFailedMessages(): Flow<List<SmsMessageEntity>>

    @Query("SELECT * FROM sms_messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): SmsMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SmsMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<SmsMessageEntity>)

    @Update
    suspend fun updateMessage(message: SmsMessageEntity)

    @Query("UPDATE sms_messages SET status = :newStatus, lastAttemptTimestamp = :timestamp WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, newStatus: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM sms_messages")
    fun getTotalMessageCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sms_messages WHERE status = :status")
    fun getMessageCountByStatus(status: String): Flow<Int>

    // Attempts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: SmsAttemptEntity)

    @Query("SELECT * FROM sms_attempts WHERE messageId = :messageId ORDER BY timestamp DESC")
    fun getAttemptsForMessage(messageId: String): Flow<List<SmsAttemptEntity>>

    // Batches
    @Query("SELECT * FROM sms_batches ORDER BY startTime DESC")
    fun getAllBatches(): Flow<List<SmsBatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: SmsBatchEntity)

    @Update
    suspend fun updateBatch(batch: SmsBatchEntity)

    // Retry Config
    @Query("SELECT * FROM retry_config WHERE id = 1")
    fun getRetryConfig(): Flow<RetryConfigEntity?>

    @Query("SELECT * FROM retry_config WHERE id = 1")
    suspend fun getRetryConfigSync(): RetryConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRetryConfig(config: RetryConfigEntity)

    // Sync Events
    @Query("SELECT * FROM sync_events WHERE synced = 0")
    suspend fun getUnsyncedEvents(): List<SyncEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncEvent(event: SyncEventEntity)

    @Query("UPDATE sync_events SET synced = 1 WHERE eventId = :eventId")
    suspend fun markEventSynced(eventId: String)
}
