package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_config")
data class DeviceConfigEntity(
    @PrimaryKey val id: Int = 1,
    val serverBaseUrl: String = "https://api.smsgateway.example.com",
    val apiEndpoint: String = "/v1/gateway",
    val apiToken: String = "",
    val deviceName: String = android.os.Build.MODEL,
    val deviceIdentifier: String = android.os.Build.SERIAL.ifBlank { "ANDROID_GATEWAY_DEFAULT" },
    val pollingIntervalMinutes: Int = 5,
    val maxBatchSize: Int = 50,
    val sendingDelayMs: Long = 2000L,
    val preferredSimSlot: Int = -1,
    val automaticSync: Boolean = true,
    val automaticCampaignProcessing: Boolean = true,
    val lastSuccessfulSync: Long = 0L,
    val lastHeartbeat: Long = 0L
)
