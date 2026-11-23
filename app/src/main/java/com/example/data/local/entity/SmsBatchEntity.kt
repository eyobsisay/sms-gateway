package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_batches")
data class SmsBatchEntity(
    @PrimaryKey val batchId: String,
    val campaignId: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0L,
    val totalSms: Int = 0,
    val sentCount: Int = 0,
    val deliveredCount: Int = 0,
    val failedCount: Int = 0,
    val pendingCount: Int = 0,
    val unknownCount: Int = 0,
    val retryCount: Int = 0
)
