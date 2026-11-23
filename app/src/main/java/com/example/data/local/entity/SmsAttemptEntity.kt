package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_attempts")
data class SmsAttemptEntity(
    @PrimaryKey val attemptId: String,
    val messageId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String,
    val failureReason: String? = null,
    val simSlot: Int = -1
)
