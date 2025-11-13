package com.cs407.capstone.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cs407.capstone.viewModel.AccountViewModel
import com.cs407.capstone.viewModel.PredictViewModel

@Composable
fun PredictScreen(predictViewModel: PredictViewModel, accountViewModel: AccountViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Fraud Detection System",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "This app monitors Gmail notifications for transaction data and automatically processes them for fraud detection.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "• Login to your account",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "• View transactions in Account tab",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "• Update settings as needed",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
