package com.cs407.capstone.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.cs407.capstone.viewModel.Notification

@Composable
fun NotificationScreen(notifications: List<Notification>) {
    val context = LocalContext.current
    val isPermissionGranted = remember {
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    }

    if (isPermissionGranted) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(notifications) { notification ->
                NotificationItem(notification)
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Please grant notification access to use this feature.")
            Button(onClick = {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                context.startActivity(intent)
            }) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(text = notification.title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        Text(text = notification.text, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
    }
}
