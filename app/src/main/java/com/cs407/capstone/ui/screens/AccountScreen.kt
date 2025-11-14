/**
 * AccountScreen.kt
 * 
 * Main account management screen with tabbed interface for:
 * - Account details display
 * - Settings management (phone number, fraud threshold)
 * - Transaction history with pull-to-refresh
 * 
 * Handles both logged-in and logged-out states with modern UI components
 */
package com.cs407.capstone.ui.screens

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.capstone.ui.components.GradientButton
import com.cs407.capstone.ui.components.ModernCard
import com.cs407.capstone.ui.components.ModernTextField
import com.cs407.capstone.viewModel.AccountViewModel
import com.cs407.capstone.viewModel.PredictViewModel

/**
 * Main account screen that switches between logged-in and logged-out states
 * - Logged in: Shows tabbed interface with account details, settings, and transactions
 * - Logged out: Shows sign-in and create account forms with tabs
 */
@Composable
fun AccountScreen(accountViewModel: AccountViewModel = viewModel()) {
    if (accountViewModel.isLoggedIn) {
        LoggedInSection(accountViewModel)
    } else {
        // Scrollable container for login/signup forms
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Allows scrolling for long forms
        ) {
            LoggedOutSection(accountViewModel)
        }
    }
}

/**
 * Logged-in user interface with three main tabs:
 * 1. Account Details - Shows user info and logout button
 * 2. Settings - Allows updating phone number and fraud threshold
 * 3. Transactions - Lists recent transactions with pull-to-refresh
 */
@Composable
fun LoggedInSection(accountViewModel: AccountViewModel) {
    var selectedTab by remember { mutableStateOf(0) } // Track active tab
    val tabs = listOf("Account", "Settings", "Transactions")
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Tab navigation bar
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Tab content based on selection
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

/**
 * Transactions tab with pull-to-refresh functionality
 * - Automatically loads transactions when user logs in
 * - Swipe down to refresh transaction list
 * - Shows enhanced transaction cards with fraud scores and status
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionsTab(accountViewModel: AccountViewModel) {
    val predictViewModel: PredictViewModel = viewModel()
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Auto-load transactions when user logs in
    LaunchedEffect(accountViewModel.loggedInAccount) {
        accountViewModel.loggedInAccount?.accountId?.let { 
            predictViewModel.getRecentTransactions(it) 
        }
    }
    
    // Pull-to-refresh state management
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            accountViewModel.loggedInAccount?.accountId?.let { accountId ->
                predictViewModel.getRecentTransactions(accountId)
                isRefreshing = false
            }
        }
    )
    
    // Container with pull-to-refresh capability
    Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
        // Scrollable list of transactions
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(predictViewModel.recentTransactions) { transaction ->
                EnhancedTransactionItem(transaction)
            }
        }
        
        // Pull-to-refresh indicator
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

/**
 * Enhanced transaction card displaying:
 * - Merchant name and category
 * - Transaction amount
 * - Fraud detection score
 * - Processing status (Pending/Processed)
 * - Fraud marking if applicable
 */
@Composable
fun EnhancedTransactionItem(transaction: com.cs407.capstone.data.RecentTransaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top row: Merchant info and amount
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    // Merchant name (primary text)
                    Text(
                        text = transaction.merchant,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    // Category (secondary text, uppercase)
                    Text(
                        text = transaction.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                // Transaction amount (right-aligned)
                Text(
                    text = "$${transaction.amount}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Bottom row: Fraud score and status
            Row(modifier = Modifier.fillMaxWidth()) {
                // Fraud detection score (formatted to 2 decimals)
                Text(
                    text = "Fraud Score: ${String.format("%.2f", transaction.fraudScore)}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                // Processing status with color coding
                Text(
                    text = if (transaction.awaitingResponse) "Pending" else "Processed",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (transaction.awaitingResponse) Color(0xFFFF9800) else Color(0xFF4CAF50)
                )
            }
            
            // Fraud warning (only shown if customer marked as fraud)
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
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Sign In", "Create Account")
    
    Column {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        ModernCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            when (selectedTab) {
                0 -> SignInForm(accountViewModel)
                1 -> CreateAccountForm(accountViewModel)
            }
        }

        if (accountViewModel.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
        }

        accountViewModel.errorMessage?.let {
            Text(
                text = it,
                color = if (it.contains("successful")) Color.Green else Color.Red,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
            )
        }
    }
}

@Composable
fun SignInForm(accountViewModel: AccountViewModel) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    Column {
        Text("Sign In", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        ModernTextField(
            value = accountViewModel.identifier,
            onValueChange = { accountViewModel.identifier = it },
            label = "Username or Email",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        ModernTextField(
            value = accountViewModel.loginPassword,
            onValueChange = { accountViewModel.loginPassword = it },
            label = "Password",
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        GradientButton(
            text = "Sign In",
            onClick = { accountViewModel.login() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CreateAccountForm(accountViewModel: AccountViewModel) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    Column {
        Text("Create Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        ModernTextField(value = accountViewModel.username, onValueChange = { accountViewModel.username = it }, label = "Username", modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        ModernTextField(value = accountViewModel.email, onValueChange = { accountViewModel.email = it }, label = "Email", modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        ModernTextField(
            value = accountViewModel.password,
            onValueChange = { accountViewModel.password = it },
            label = "Password",
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModernTextField(value = accountViewModel.firstName, onValueChange = { accountViewModel.firstName = it }, label = "First Name", modifier = Modifier.weight(1f))
            ModernTextField(value = accountViewModel.lastName, onValueChange = { accountViewModel.lastName = it }, label = "Last Name", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        ModernTextField(value = accountViewModel.ccNum, onValueChange = { accountViewModel.ccNum = it }, label = "Credit Card Number", modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModernTextField(value = accountViewModel.gender, onValueChange = { accountViewModel.gender = it }, label = "Gender (M/F)", modifier = Modifier.weight(1f))
            ModernTextField(value = accountViewModel.dateOfBirth, onValueChange = { accountViewModel.dateOfBirth = it }, label = "DOB (YYYY-MM-DD)", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        ModernTextField(value = accountViewModel.job, onValueChange = { accountViewModel.job = it }, label = "Job", modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        ModernTextField(value = accountViewModel.street, onValueChange = { accountViewModel.street = it }, label = "Street", modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModernTextField(value = accountViewModel.city, onValueChange = { accountViewModel.city = it }, label = "City", modifier = Modifier.weight(1f))
            ModernTextField(value = accountViewModel.state, onValueChange = { accountViewModel.state = it }, label = "State", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        ModernTextField(value = accountViewModel.zip, onValueChange = { accountViewModel.zip = it }, label = "Zip Code", modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        ModernTextField(value = accountViewModel.phoneNumber, onValueChange = { accountViewModel.phoneNumber = it }, label = "Phone Number", modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = accountViewModel.smsOptIn, onCheckedChange = { accountViewModel.smsOptIn = it })
            Text("Opt-in for SMS notifications")
        }
        Spacer(modifier = Modifier.height(16.dp))
        GradientButton(
            text = "Create Account",
            onClick = { accountViewModel.createAccount() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
