package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_messages")
data class SmsMessageEntity(
    @PrimaryKey val id: String,
    val campaignId: String,
    val recipientName: String,
    val phoneNumber: String,
    val body: String,
    val priority: Int = 0,
    val scheduledTime: Long = 0L,
    val preferredSim: Int = -1,
    val status: String = "QUEUED", // QUEUED, PROCESSING, SENT, DELIVERED, SEND_FAILED, DELIVERY_FAILED, DELIVERY_PENDING, DELIVERY_UNKNOWN, RETRY_QUEUED, RETRY_PROCESSING
    val sendingTimestamp: Long = 0L,
    val deliveryTimestamp: Long = 0L,
    val lastAttemptTimestamp: Long = 0L,
    val retryCount: Int = 0,
    val failureReason: String? = null,
    val simUsed: Int = -1
)
