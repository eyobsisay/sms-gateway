package com.example.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import com.example.repository.SmsGatewayRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsSentReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getStringExtra("EXTRA_MESSAGE_ID") ?: return
        val repo = SmsGatewayRepository(context)

        val status = when (resultCode) {
            Activity.RESULT_OK -> "SENT"
            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "SEND_FAILED: Generic Failure"
            SmsManager.RESULT_ERROR_RADIO_OFF -> "SEND_FAILED: Radio Off"
            SmsManager.RESULT_ERROR_NULL_PDU -> "SEND_FAILED: Null PDU"
            SmsManager.RESULT_ERROR_NO_SERVICE -> "SEND_FAILED: No Service"
            else -> "SEND_FAILED: Error code $resultCode"
        }

        scope.launch {
            try {
                if (status == "SENT") {
                    repo.updateMessageStatus(messageId, "SENT")
                } else {
                    repo.updateMessageStatus(messageId, "SEND_FAILED", status)
                }
                repo.syncPendingEvents()
            } catch (e: Exception) {
                Log.e("SmsSentReceiver", "Error handling sent broadcast for $messageId", e)
            }
        }
    }
}
