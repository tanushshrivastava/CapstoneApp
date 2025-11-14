/**
 * NotificationListener.kt
 * 
 * Android NotificationListenerService that monitors Gmail notifications for transaction data.
 * 
 * Key Features:
 * - Listens for Gmail notifications containing JSON transaction data
 * - Extracts merchant transaction info (amount, merchant, category, location)
 * - Adds logged-in user's accountId and sends to backend API
 * - Prevents duplicate requests with 30-second deduplication window
 * - Backend auto-populates all other transaction fields from user account
 * 
 * Expected Gmail JSON format:
 * {
 *   "amt": 99.99,
 *   "merchant": "Amazon",
 *   "category": "shopping", 
 *   "merch_lat": 43.0731,
 *   "merch_long": -89.4012
 * }
 */
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
    
    // Deduplication system to prevent duplicate transaction requests
    private val recentRequests = mutableMapOf<String, Long>() // Maps request hash to timestamp
    private val dedupeWindowMs = 30000L // 30 seconds - ignore duplicates within this window

    /**
     * Initialize the notification listener service
     * Sets up Google Places API for location services (if needed)
     */
    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.GOOGLE_MAPS_KEY)
        }
    }

    /**
     * Handle test notification requests for debugging
     * Allows manual triggering of notification processing
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "com.cs407.capstone.ACTION_TEST_NOTIFICATION") {
            coroutineScope.launch {
                // Process test transaction for debugging
                processGmailNotification("{\"accountId\":\"test-id\",\"transaction\":{\"amt\":10.0,\"category\":\"shopping\",\"merchant\":\"Test Merchant\"}}")
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Called when any notification is posted to the system
     * Filters for Gmail notifications and extracts transaction data
     */
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val extras = notification.extras
        val packageName = sbn.packageName

        val intent = Intent("com.cs407.capstone.NOTIFICATION_LISTENER")
        intent.putExtra("title", "Notification Debug")

        // Only process Gmail notifications
        if (packageName == "com.google.android.gm") {
            // Extract all possible text fields from Gmail notification
            // Gmail may put JSON in different text fields depending on email content
            val title = extras.getCharSequence("android.title")?.toString() ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            val bigText = extras.getCharSequence("android.bigText")?.toString() ?: ""
            val subText = extras.getCharSequence("android.subText")?.toString() ?: ""
            val summaryText = extras.getCharSequence("android.summaryText")?.toString() ?: ""
            
            // Combine all text fields to search for JSON
            val fullContent = "$title $text $bigText $subText $summaryText"
            Log.d("NotificationListener", "Full Gmail content: $fullContent")
            
            // Process in background thread
            coroutineScope.launch {
                processGmailNotification(fullContent)
            }
        } else {
            // Log non-Gmail notifications for debugging
            intent.putExtra("text", "Received notification from: $packageName")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }


    /**
     * Process Gmail notification text to extract and submit transaction data
     * 
     * Flow:
     * 1. Extract JSON from notification text
     * 2. Check for duplicates (30-second window)
     * 3. Add logged-in user's accountId
     * 4. Send to backend API
     * 5. Backend auto-populates remaining fields from user account
     */
    private fun processGmailNotification(text: String) {
        try {
            // Find JSON boundaries in the notification text
            val jsonStart = text.indexOf("{")
            val jsonEnd = text.lastIndexOf("}") + 1
            
            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                Log.e("NotificationListener", "No JSON found in notification")
                return
            }
            
            // Extract and clean JSON (replace HTML entities)
            val jsonString = text.substring(jsonStart, jsonEnd).replace("&quot;", "\"")
            Log.d("NotificationListener", "Processing JSON: $jsonString")
            
            // DEDUPLICATION: Create unique key from JSON content
            val requestKey = jsonString.hashCode().toString()
            val currentTime = System.currentTimeMillis()
            
            // Check if we've processed this exact request recently
            val lastRequestTime = recentRequests[requestKey]
            if (lastRequestTime != null && (currentTime - lastRequestTime) < dedupeWindowMs) {
                Log.d("NotificationListener", "Duplicate request ignored")
                return
            }
            
            // Clean up old entries to prevent memory leaks
            recentRequests.entries.removeAll { (currentTime - it.value) > dedupeWindowMs }
            
            // Record this request timestamp
            recentRequests[requestKey] = currentTime
            
            // Get logged-in user's account ID from SharedPreferences
            val sharedPreferences = getSharedPreferences("account_prefs", android.content.Context.MODE_PRIVATE)
            val accountId = sharedPreferences.getString("account_id", null)
            
            if (accountId == null) {
                Log.e("NotificationListener", "No account ID found")
                return
            }
            
            // Wrap merchant transaction JSON with accountId
            // Format: {"accountId":"...", "transaction":{merchant data}}
            val fullRequest = "{\"accountId\":\"$accountId\",\"transaction\":$jsonString}"
            
            // Send to backend API
            coroutineScope.launch {
                try {
                    val requestBody = fullRequest.toRequestBody("application/json".toMediaType())
                    val response = RetrofitClient.apiService.postTransactionFromJson(requestBody)
                    
                    // Broadcast result to app UI
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
                    // Handle network/API errors
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
