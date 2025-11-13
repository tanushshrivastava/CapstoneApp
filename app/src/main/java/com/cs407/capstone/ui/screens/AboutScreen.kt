package com.cs407.capstone.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Fraud Detection System", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "This application monitors Gmail notifications for transaction data and automatically processes them for fraud detection.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("How it works:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("• Login to your account")
        Text("• View transactions in Account tab")
        Text("• Update settings as needed")
        Text("• Gmail notifications are automatically processed")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Team:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("- Tanus Sharma")
    }
}
