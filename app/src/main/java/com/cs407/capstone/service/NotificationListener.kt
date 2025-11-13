package com.cs407.capstone.service

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cs407.capstone.BuildConfig
import com.cs407.capstone.api.RetrofitClient
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


class NotificationListener : NotificationListenerService() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.GOOGLE_MAPS_KEY)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "com.cs407.capstone.ACTION_TEST_NOTIFICATION") {
            coroutineScope.launch {
                processGmailNotification("{\"accountId\":\"test-id\",\"transaction\":{\"amt\":10.0,\"category\":\"shopping\",\"merchant\":\"Test Merchant\"}}")
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val extras = notification.extras
        val packageName = sbn.packageName

        val intent = Intent("com.cs407.capstone.NOTIFICATION_LISTENER")
        intent.putExtra("title", "Notification Debug")

        if (packageName == "com.google.android.gm") {
            // Extract all possible text from Gmail notification
            val title = extras.getCharSequence("android.title")?.toString() ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            val bigText = extras.getCharSequence("android.bigText")?.toString() ?: ""
            val subText = extras.getCharSequence("android.subText")?.toString() ?: ""
            val summaryText = extras.getCharSequence("android.summaryText")?.toString() ?: ""
            
            val fullContent = "$title $text $bigText $subText $summaryText"
            Log.d("NotificationListener", "Full Gmail content: $fullContent")
            
            coroutineScope.launch {
                processGmailNotification(fullContent)
            }
        } else {
            intent.putExtra("text", "Received notification from: $packageName")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }


    private fun processGmailNotification(text: String) {
        try {
            // Parse JSON from Gmail notification
            val jsonStart = text.indexOf("{")
            val jsonEnd = text.lastIndexOf("}") + 1
            
            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                Log.e("NotificationListener", "No JSON found in notification")
                return
            }
            
            val jsonString = text.substring(jsonStart, jsonEnd)
                .replace("&quot;", "\"")
            
            Log.d("NotificationListener", "Processing JSON: $jsonString")
            
            coroutineScope.launch {
                try {
                    val requestBody = jsonString.toRequestBody("application/json".toMediaType())
                    val response = RetrofitClient.apiService.postTransactionFromJson(requestBody)
                    val intent = Intent("com.cs407.capstone.NOTIFICATION_LISTENER")
                    if (response.isSuccessful) {
                        intent.putExtra("title", "Transaction Processed")
                        intent.putExtra("text", "Gmail transaction processed successfully")
                    } else {
                        intent.putExtra("title", "Transaction Failed")
                        intent.putExtra("text", "Error: ${response.errorBody()?.string()}")
                        Log.e("NotificationListener", "Error sending transaction: ${response.errorBody()?.string()}")
                    }
                    LocalBroadcastManager.getInstance(this@NotificationListener).sendBroadcast(intent)
                } catch (e: Exception) {
                    val intent = Intent("com.cs407.capstone.NOTIFICATION_LISTENER")
                    intent.putExtra("title", "Transaction Failed")
                    intent.putExtra("text", "Exception: ${e.message}")
                    LocalBroadcastManager.getInstance(this@NotificationListener).sendBroadcast(intent)
                    Log.e("NotificationListener", "Exception processing transaction", e)
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationListener", "Error parsing Gmail notification", e)
        }
    }


}
