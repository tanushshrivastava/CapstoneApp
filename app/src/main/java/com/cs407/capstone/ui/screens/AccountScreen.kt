package com.cs407.capstone.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.capstone.viewModel.AccountViewModel
import com.cs407.capstone.viewModel.PredictViewModel

@Composable
fun AccountScreen(accountViewModel: AccountViewModel = viewModel()) {
    if (accountViewModel.isLoggedIn) {
        LoggedInSection(accountViewModel)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            LoggedOutSection(accountViewModel)
        }
    }
}

@Composable
fun LoggedInSection(accountViewModel: AccountViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Account", "Settings", "Transactions")
    
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        when (selectedTab) {
            0 -> AccountDetailsTab(accountViewModel)
            1 -> SettingsTab(accountViewModel)
            2 -> TransactionsTab(accountViewModel)
        }
    }
}

@Composable
fun AccountDetailsTab(accountViewModel: AccountViewModel) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Account Details", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Username: ${accountViewModel.loggedInAccount?.username}")
            Text("Account ID: ${accountViewModel.loggedInAccount?.accountId}")
            Text("Phone: ${accountViewModel.phoneNumber}")
            Text("Fraud Threshold: ${accountViewModel.fraudThreshold}")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { accountViewModel.logout() }) {
                Text("Sign Out")
            }
        }
    }
}

@Composable
fun SettingsTab(accountViewModel: AccountViewModel) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = accountViewModel.phoneNumber,
                onValueChange = { accountViewModel.phoneNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = accountViewModel.fraudThreshold.toString(),
                onValueChange = { accountViewModel.fraudThreshold = it.toDoubleOrNull() ?: 0.0 },
                label = { Text("Fraud Threshold") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { accountViewModel.updateAccountSettings() }) {
                Text("Update Settings")
            }
        }
    }
}

@Composable
fun TransactionsTab(accountViewModel: AccountViewModel) {
    val predictViewModel: PredictViewModel = viewModel()
    
    LaunchedEffect(accountViewModel.loggedInAccount) {
        accountViewModel.loggedInAccount?.accountId?.let { 
            predictViewModel.getRecentTransactions(it) 
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(predictViewModel.recentTransactions) { transaction ->
            EnhancedTransactionItem(transaction)
        }
    }
}

@Composable
fun EnhancedTransactionItem(transaction: com.cs407.capstone.data.RecentTransaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.merchant,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = transaction.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = "$${transaction.amount}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Fraud Score: ${String.format("%.2f", transaction.fraudScore)}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (transaction.awaitingResponse) "Pending" else "Processed",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (transaction.awaitingResponse) Color(0xFFFF9800) else Color(0xFF4CAF50)
                )
            }
            
            if (transaction.customerMarkedFraud) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠️ Marked as Fraud",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun LoggedOutSection(accountViewModel: AccountViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SignInForm(accountViewModel)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                CreateAccountForm(accountViewModel)
            }
        }

        if (accountViewModel.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        accountViewModel.errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = if (it.contains("successful")) Color.Green else Color.Red)
        }
    }
}

@Composable
fun SignInForm(accountViewModel: AccountViewModel) {
    Column {
        Text("Sign In", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = accountViewModel.identifier,
            onValueChange = { accountViewModel.identifier = it },
            label = { Text("Username or Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = accountViewModel.loginPassword,
            onValueChange = { accountViewModel.loginPassword = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { accountViewModel.login() }) {
            Text("Sign In")
        }
    }
}

@Composable
fun CreateAccountForm(accountViewModel: AccountViewModel) {
    Column {
        Text("Create Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = accountViewModel.username,
            onValueChange = { accountViewModel.username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = accountViewModel.email,
            onValueChange = { accountViewModel.email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = accountViewModel.password,
            onValueChange = { accountViewModel.password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = accountViewModel.phoneNumber,
            onValueChange = { accountViewModel.phoneNumber = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = accountViewModel.address,
            onValueChange = { accountViewModel.address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { accountViewModel.createAccount() }) {
            Text("Create Account")
        }
    }
}
