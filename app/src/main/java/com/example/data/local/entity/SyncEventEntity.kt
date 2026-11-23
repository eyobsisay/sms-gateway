package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_events")
data class SyncEventEntity(
    @PrimaryKey val eventId: String,
    val messageId: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
