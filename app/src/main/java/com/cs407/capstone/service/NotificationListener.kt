package com.cs407.capstone.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cs407.capstone.BuildConfig
import com.cs407.capstone.api.RetrofitClient
import com.cs407.capstone.data.Transaction
import com.cs407.capstone.data.TransactionRequest
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

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
                processNotification("amt=10.0,category=shopping,merchant=Test Merchant,cc_num=1234567890123456,first=John,last=Doe,gender=M,dob=1990-01-01,job=Software Engineer,street=123 Main St,city=Anytown,state=CA,zip=12345,merch_lat=34.0522,merch_long=-118.2437")
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val extras = notification.extras
        val text = extras.getCharSequence("android.text")?.toString()

        val intent = Intent("com.cs407.capstone.NOTIFICATION_LISTENER")
        intent.putExtra("title", "Notification Debug")

        if (text != null) {
            // Directly process any notification with text
            coroutineScope.launch {
                processNotification(text)
            }
        } else {
            // Handle case where notification text is null
            intent.putExtra("text", "Received notification with null text.")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }


    private fun processNotification(text: String) {
        val sharedPreferences = getSharedPreferences("account_prefs", Context.MODE_PRIVATE)
        val accountId = sharedPreferences.getString("account_id", null) ?: return

        val data = parseNotificationText(text)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            val lat = location?.latitude ?: 0.0
            val long = location?.longitude ?: 0.0
            val currentTime = System.currentTimeMillis()
            val transDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(currentTime))

            val transaction = Transaction(
                trans_date_trans_time = transDate,
                cc_num = data["cc_num"] ?: "",
                amt = data["amt"]?.toDoubleOrNull() ?: 0.0,
                category = data["category"] ?: "",
                merchant = data["merchant"] ?: "",
                first = data["first"] ?: "",
                last = data["last"] ?: "",
                gender = data["gender"] ?: "",
                dob = data["dob"] ?: "",
                job = data["job"] ?: "",
                street = data["street"] ?: "",
                city = data["city"] ?: "",
                state = data["state"] ?: "",
                zip = data["zip"] ?: "",
                lat = lat,
                long = long,
                city_pop = 0, // Placeholder
                merch_lat = data["merch_lat"]?.toDoubleOrNull() ?: 0.0,
                merch_long = data["merch_long"]?.toDoubleOrNull() ?: 0.0,
                trans_num = UUID.randomUUID().toString(),
                unix_time = currentTime / 1000
            )

            coroutineScope.launch {
                try {
                    val request = TransactionRequest(accountId, transaction)
                    val response = RetrofitClient.apiService.postTransaction(request)
                    val intent = Intent("com.cs407.capstone.NOTIFICATION_LISTENER")
                    if (response.isSuccessful) {
                        intent.putExtra("title", "Transaction Sent")
                        intent.putExtra("text", "Transaction sent successfully")
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
                    Log.e("NotificationListener", "Exception sending transaction", e)
                }
            }
        }
    }

    private fun parseNotificationText(text: String): Map<String, String> {
        val data = mutableMapOf<String, String>()
        text.split(",").forEach { part ->
            val keyValue = part.trim().split("=", limit = 2)
            if (keyValue.size == 2) {
                data[keyValue[0]] = keyValue[1]
            }
        }
        return data
    }
}
