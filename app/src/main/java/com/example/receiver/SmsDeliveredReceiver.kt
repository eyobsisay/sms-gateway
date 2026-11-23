package com.example.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.repository.SmsGatewayRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsDeliveredReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getStringExtra("EXTRA_MESSAGE_ID") ?: return
        val repo = SmsGatewayRepository(context)

        val status = if (resultCode == Activity.RESULT_OK) {
            "DELIVERED"
        } else {
            "DELIVERY_FAILED"
        }

        scope.launch {
            try {
                repo.updateMessageStatus(messageId, status)
                repo.syncPendingEvents()
            } catch (e: Exception) {
                Log.e("SmsDeliveredReceiver", "Error handling delivery broadcast for $messageId", e)
            }
        }
    }
}
