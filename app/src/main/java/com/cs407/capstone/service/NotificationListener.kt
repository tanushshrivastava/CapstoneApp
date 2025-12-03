package com.cs407.capstone.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.cs407.capstone.BuildConfig
import com.cs407.capstone.api.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class NotificationListener : NotificationListenerService() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var openAI: OpenAI

    private val recentRequests = mutableMapOf<String, Long>()
    private val dedupeWindowMs = 30000L

    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationListener", "Service created. Initializing OpenAI.")
        openAI = OpenAI(BuildConfig.OPEN_AI_KEY)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d("NotificationListener", "onNotificationPosted received from package: ${sbn.packageName}")
        val notification = sbn.notification
        val extras = notification.extras

        // Combine all text fields for robust parsing
        val title = extras.getCharSequence("android.title")?.toString()
        val text = extras.getCharSequence("android.text")?.toString()
        val bigText = extras.getCharSequence("android.bigText")?.toString()
        val subText = extras.getCharSequence("android.subText")?.toString()
        val summaryText = extras.getCharSequence("android.summaryText")?.toString()

        val notificationContent = listOfNotNull(title, text, bigText, subText, summaryText).joinToString(" | ")

        if (notificationContent.isBlank()) {
            Log.d("NotificationListener", "Notification content is completely blank. Ignoring.")
            return
        }

        Log.d("NotificationListener", "Processing combined notification content:\n$notificationContent")

        coroutineScope.launch {
            try {
                val chatCompletionRequest = ChatCompletionRequest(
                    model = ModelId("gpt-3.5-turbo"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.System,
                            content = """You are a financial transaction parser. Analyze the notification text. 
                            If it is a purchase or financial transaction, extract merchant, amount (as a number), and optionally merchant latitude and longitude. 
                            Return the result as a JSON object with the keys: 'merchant', 'amt', 'merch_lat', 'merch_long'.
                            For example, for 'a transaction was made at Amazon of price 100.00 and at location 43,89', you should return '{"merchant": "Amazon", "amt": 100.00, "merch_lat": 43, "merch_long": 89}'.
                            If it is not a financial transaction, return an empty JSON object {}. If the notification includes Google Voice or the number 877-791-3403 or 608-620-3659 then also return {}."""
                        ),
                        ChatMessage(
                            role = ChatRole.User,
                            content = notificationContent
                        )
                    )
                )
                Log.d("NotificationListener", "Sending request to OpenAI...")
                val completion = openAI.chatCompletion(chatCompletionRequest)
                Log.d("NotificationListener", "OpenAI request completed.")
                val jsonResponse = completion.choices.first().message.content

                if (jsonResponse != null && jsonResponse.trim().length > 2) { // not an empty JSON
                    Log.d("NotificationListener", "OpenAI response contains data: $jsonResponse")
                    sendTransactionToApi(jsonResponse)
                } else {
                    Log.d("NotificationListener", "OpenAI response is empty or not a transaction. Ignoring. Response: $jsonResponse")
                }
            } catch (e: Exception) {
                Log.e("NotificationListener", "Error processing notification with OpenAI", e)
            }
        }
    }

    private fun sendTransactionToApi(transactionJson: String) {
        Log.d("NotificationListener", "Preparing to send transaction to API.")
        val requestKey = transactionJson.hashCode().toString()
        val currentTime = System.currentTimeMillis()

        if (recentRequests.containsKey(requestKey) && (currentTime - (recentRequests[requestKey] ?: 0) < dedupeWindowMs)) {
            Log.d("NotificationListener", "Duplicate transaction ignored (hash: $requestKey).")
            return
        }
        recentRequests[requestKey] = currentTime

        val sharedPreferences = getSharedPreferences("account_prefs", MODE_PRIVATE)
        val accountId = sharedPreferences.getString("account_id", null)

        if (accountId == null) {
            Log.e("NotificationListener", "No account ID found. User may not be logged in. Cannot send transaction.")
            return
        }
        Log.d("NotificationListener", "Found accountId: $accountId")

        // Re-create the JSON object to include the accountId
        val transaction = JSONObject(transactionJson)
        val finalPayload = JSONObject()
        finalPayload.put("accountId", accountId)
        finalPayload.put("transaction", transaction)

        val payloadString = finalPayload.toString()
        Log.d("NotificationListener", "Final payload to be sent: $payloadString")

        val requestBody = payloadString.toRequestBody("application/json".toMediaType())

        coroutineScope.launch {
            try {
                Log.d("NotificationListener", "Executing API call...")
                val response = RetrofitClient.apiService.postTransactionFromJson(requestBody)
                if (response.isSuccessful) {
                    Log.d("NotificationListener", "API Success: Transaction sent successfully. Response: ${response.body()?.toString()}")
                } else {
                    Log.e("NotificationListener", "API Error: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("NotificationListener", "Exception while sending transaction to API", e)
            }
        }
    }
}
