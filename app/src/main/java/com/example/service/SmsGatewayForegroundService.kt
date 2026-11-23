package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.repository.SmsGatewayRepository
import kotlinx.coroutines.*

class SmsGatewayForegroundService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var repository: SmsGatewayRepository
    private lateinit var sendingEngine: SmsSendingEngine
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        repository = SmsGatewayRepository(applicationContext)
        sendingEngine = SmsSendingEngine(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = createNotification("SMS Gateway running in background")
        startForeground(1001, notification)

        if (!isRunning) {
            isRunning = true
            startQueueProcessor()
        }

        return START_STICKY
    }

    private fun startQueueProcessor() {
        serviceScope.launch {
            while (isActive) {
                try {
                    val queued = repository.getQueuedMessages(10)
                    for (msg in queued) {
                        repository.updateMessageStatus(msg.id, "PROCESSING")
                        val result = sendingEngine.sendSmsMessage(msg, msg.preferredSim)
                        if (result.isFailure) {
                            repository.updateMessageStatus(msg.id, "SEND_FAILED", result.exceptionOrNull()?.message ?: "Unknown error")
                        }
                        val config = repository.deviceConfig
                        // delay between messages
                        delay(2000L)
                    }
                    repository.syncPendingEvents()
                } catch (e: Exception) {
                    // ignore
                }
                delay(10000L) // check queue every 10 seconds
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "sms_gateway_channel",
                "SMS Gateway Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, "sms_gateway_channel")
            .setContentTitle("SMS Gateway Active")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_send)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        isRunning = false
    }
}
