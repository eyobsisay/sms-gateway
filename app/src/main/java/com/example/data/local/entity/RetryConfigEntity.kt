package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "retry_config")
data class RetryConfigEntity(
    @PrimaryKey val id: Int = 1,
    val autoRetryEnabled: Boolean = false,
    val maxAttempts: Int = 3,
    val retryDelayMinutes: Long = 5L,
    val maxRetryBatchSize: Int = 100,
    val sendingMode: String = "Sequential"
)
