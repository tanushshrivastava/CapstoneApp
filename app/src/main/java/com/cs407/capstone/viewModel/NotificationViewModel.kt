package com.cs407.capstone.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

data class Notification(val title: String, val text: String)

class NotificationViewModel : ViewModel() {
    val notifications = mutableStateListOf<Notification>()

    fun addNotification(notification: Notification) {
        notifications.add(notification)
    }
}
