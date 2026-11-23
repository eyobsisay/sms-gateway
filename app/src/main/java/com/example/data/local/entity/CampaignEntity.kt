package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaigns")
data class CampaignEntity(
    @PrimaryKey val campaignId: String,
    val title: String,
    val totalMessages: Int,
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis()
)
