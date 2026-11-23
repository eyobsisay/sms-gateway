package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.*
import com.example.repository.SmsGatewayRepository
import com.example.service.SmsSendingEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SmsGatewayViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SmsGatewayRepository(application)
    private val sendingEngine = SmsSendingEngine(application)

    val deviceConfig: StateFlow<DeviceConfigEntity?> = repository.deviceConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val campaigns: StateFlow<List<CampaignEntity>> = repository.campaigns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messages: StateFlow<List<SmsMessageEntity>> = repository.messages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val failedMessages: StateFlow<List<SmsMessageEntity>> = repository.failedMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val batches: StateFlow<List<SmsBatchEntity>> = repository.batches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val retryConfig: StateFlow<RetryConfigEntity?> = repository.retryConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalCount: StateFlow<Int> = repository.totalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val sentCount: StateFlow<Int> = repository.sentCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val deliveredCount: StateFlow<Int> = repository.deliveredCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val failedCount: StateFlow<Int> = repository.failedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val queuedCount: StateFlow<Int> = repository.queuedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isSendingActive = MutableStateFlow(false)
    val isSendingActive: StateFlow<Boolean> = _isSendingActive.asStateFlow()

    fun saveDeviceConfig(config: DeviceConfigEntity) {
        viewModelScope.launch {
            repository.saveDeviceConfig(config)
            _statusMessage.value = "Configuration saved successfully."
        }
    }

    fun saveRetryConfig(config: RetryConfigEntity) {
        viewModelScope.launch {
            repository.saveRetryConfig(config)
            _statusMessage.value = "Retry configuration saved."
        }
    }

    fun testConnectionAndRegister() {
        viewModelScope.launch {
            _statusMessage.value = "Registering device with gateway server..."
            val result = repository.testConnectionAndRegister()
            _statusMessage.value = if (result.isSuccess) {
                result.getOrNull() ?: "Registration successful."
            } else {
                "Error: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun fetchCampaignJobs() {
        viewModelScope.launch {
            _statusMessage.value = "Fetching campaign jobs..."
            val result = repository.fetchAndStoreCampaignJobs()
            _statusMessage.value = if (result.isSuccess) {
                "Fetched ${result.getOrDefault(0)} new campaign jobs."
            } else {
                "Fetch error: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun startPullAndSendCampaign() {
        if (_isSendingActive.value) return
        viewModelScope.launch {
            _isSendingActive.value = true
            _statusMessage.value = "Pulling latest campaign jobs from server..."
            
            val fetchResult = repository.fetchAndStoreCampaignJobs()
            val fetchedCount = if (fetchResult.isSuccess) fetchResult.getOrDefault(0) else 0
            
            val queuedList = repository.getQueuedMessages(100)
            if (queuedList.isEmpty()) {
                _statusMessage.value = "Fetched $fetchedCount jobs. No queued messages to send."
                _isSendingActive.value = false
                return@launch
            }

            _statusMessage.value = "Starting campaign dispatch: ${queuedList.size} messages in queue."
            val config = repository.deviceConfigSync
            val delayMs = config?.sendingDelayMs ?: 2000L

            var successCount = 0
            var failCount = 0

            for ((index, msg) in queuedList.withIndex()) {
                _statusMessage.value = "Sending message ${index + 1} of ${queuedList.size} to ${msg.phoneNumber}..."
                repository.updateMessageStatus(msg.id, "PROCESSING")

                val result = sendingEngine.sendSmsMessage(msg, msg.preferredSim)
                if (result.isSuccess) {
                    repository.updateMessageStatus(msg.id, "SENT")
                    successCount++
                } else {
                    val errorReason = result.exceptionOrNull()?.message ?: "Unknown error"
                    repository.updateMessageStatus(msg.id, "SEND_FAILED", errorReason)
                    failCount++
                }

                // Sync status immediately back to server
                repository.syncPendingEvents()

                // Respect configured interval delay between messages
                delay(delayMs)
            }

            _statusMessage.value = "Campaign finished! Sent: $successCount, Failed: $failCount."
            _isSendingActive.value = false
        }
    }

    fun synchronizeNow() {
        viewModelScope.launch {
            _statusMessage.value = "Synchronizing status events..."
            val result = repository.syncPendingEvents()
            _statusMessage.value = if (result.isSuccess) {
                "Synchronized ${result.getOrDefault(0)} events."
            } else {
                "Sync error: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun retryFailedMessage(messageId: String) {
        viewModelScope.launch {
            val msg = repository.getMessageById(messageId)
            if (msg != null) {
                repository.updateMessageStatus(messageId, "QUEUED")
                val result = sendingEngine.sendSmsMessage(msg, msg.preferredSim)
                if (result.isSuccess) {
                    repository.updateMessageStatus(messageId, "SENT")
                    _statusMessage.value = "Retried message successfully."
                } else {
                    repository.updateMessageStatus(messageId, "SEND_FAILED", result.exceptionOrNull()?.message)
                    _statusMessage.value = "Retry failed."
                }
            }
        }
    }

    fun retryAllFailed() {
        viewModelScope.launch {
            val failedList = failedMessages.value
            var count = 0
            for (msg in failedList) {
                repository.updateMessageStatus(msg.id, "QUEUED")
                val result = sendingEngine.sendSmsMessage(msg, msg.preferredSim)
                if (result.isSuccess) {
                    repository.updateMessageStatus(msg.id, "SENT")
                } else {
                    repository.updateMessageStatus(msg.id, "SEND_FAILED", result.exceptionOrNull()?.message)
                }
                count++
            }
            _statusMessage.value = "Retried $count failed messages."
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
