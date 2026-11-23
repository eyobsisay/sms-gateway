package com.example.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import com.example.data.local.entity.SmsMessageEntity
import com.example.receiver.SmsDeliveredReceiver
import com.example.receiver.SmsSentReceiver

class SmsSendingEngine(private val context: Context) {

    fun sendSmsMessage(message: SmsMessageEntity, preferredSimSlot: Int = -1): Result<Unit> {
        return try {
            val smsManager = getSmsManagerForSim(context, preferredSimSlot)
            
            val sentIntent = Intent(context, SmsSentReceiver::class.java).apply {
                putExtra("EXTRA_MESSAGE_ID", message.id)
            }
            val deliveredIntent = Intent(context, SmsDeliveredReceiver::class.java).apply {
                putExtra("EXTRA_MESSAGE_ID", message.id)
            }

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val sentPI = PendingIntent.getBroadcast(context, message.id.hashCode(), sentIntent, flags)
            val deliveredPI = PendingIntent.getBroadcast(context, message.id.hashCode() + 1000, deliveredIntent, flags)

            val parts = smsManager.divideMessage(message.body)
            if (parts.size > 1) {
                val sentList = ArrayList<PendingIntent>()
                val deliveredList = ArrayList<PendingIntent>()
                for (i in parts.indices) {
                    sentList.add(sentPI)
                    deliveredList.add(deliveredPI)
                }
                smsManager.sendMultipartTextMessage(message.phoneNumber, null, parts, sentList, deliveredList)
            } else {
                smsManager.sendTextMessage(message.phoneNumber, null, message.body, sentPI, deliveredPI)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getSmsManagerForSim(context: Context, simSlot: Int): SmsManager {
        return if (Build.VERSION.SDK_INT >= 22) {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            val activeSubscriptionInfoList = try {
                subscriptionManager?.activeSubscriptionInfoList
            } catch (e: SecurityException) {
                null
            }

            if (!activeSubscriptionInfoList.isNullOrEmpty() && simSlot >= 0 && simSlot < activeSubscriptionInfoList.size) {
                val subId = activeSubscriptionInfoList[simSlot].subscriptionId
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java).createForSubscriptionId(subId)
                } else {
                    SmsManager.getSmsManagerForSubscriptionId(subId)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    SmsManager.getDefault()
                }
            }
        } else {
            SmsManager.getDefault()
        }
    }
}
