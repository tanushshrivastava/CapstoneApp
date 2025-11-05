package com.cs407.capstone.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cs407.capstone.data.Transaction
import com.cs407.capstone.service.NotificationListener
import com.cs407.capstone.viewModel.AccountViewModel
import com.cs407.capstone.viewModel.PredictViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictScreen(predictViewModel: PredictViewModel, accountViewModel: AccountViewModel) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Predict", "Recent")

    LaunchedEffect(tabIndex) {
        if (tabIndex == 1) {
            accountViewModel.loggedInAccount?.accountId?.let { predictViewModel.getRecentTransactions(it) }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }
        when (tabIndex) {
            0 -> PredictForm(predictViewModel, accountViewModel)
            1 -> RecentTransactionsScreen(predictViewModel)
        }
    }
}

@Composable
fun PredictForm(viewModel: PredictViewModel, accountViewModel: AccountViewModel) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Transaction Templates")
            Row {
                viewModel.getTemplates().forEach { template ->
                    Button(onClick = { viewModel.loadTemplate(template) }) {
                        Text(template.merchant.replace("fraud_", ""))
                    }
                }
            }
        }
        item { OutlinedTextField(viewModel.transactionDate.value, { viewModel.transactionDate.value = it }, label = { Text("Transaction Date") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.ccNum.value, { viewModel.ccNum.value = it }, label = { Text("Credit Card Number") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.amount.value, { viewModel.amount.value = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.category.value, { viewModel.category.value = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.merchant.value, { viewModel.merchant.value = it }, label = { Text("Merchant") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.firstName.value, { viewModel.firstName.value = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.lastName.value, { viewModel.lastName.value = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.gender.value, { viewModel.gender.value = it }, label = { Text("Gender") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.dob.value, { viewModel.dob.value = it }, label = { Text("Date of Birth") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.job.value, { viewModel.job.value = it }, label = { Text("Job") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.street.value, { viewModel.street.value = it }, label = { Text("Street") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.city.value, { viewModel.city.value = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.state.value, { viewModel.state.value = it }, label = { Text("State") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.zip.value, { viewModel.zip.value = it }, label = { Text("Zip Code") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.lat.value, { viewModel.lat.value = it }, label = { Text("Latitude") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.long.value, { viewModel.long.value = it }, label = { Text("Longitude") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.cityPop.value, { viewModel.cityPop.value = it }, label = { Text("City Population") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.merchLat.value, { viewModel.merchLat.value = it }, label = { Text("Merchant Latitude") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.merchLong.value, { viewModel.merchLong.value = it }, label = { Text("Merchant Longitude") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.transNum.value, { viewModel.transNum.value = it }, label = { Text("Transaction Number") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(viewModel.unixTime.value, { viewModel.unixTime.value = it }, label = { Text("Unix Time") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Button(onClick = { accountViewModel.loggedInAccount?.accountId?.let { viewModel.sendPredictionRequest(it) } }, modifier = Modifier.fillMaxWidth()) {
                Text("Predict")
            }
        }
        item {
            Button(onClick = {
                val intent = Intent(context, NotificationListener::class.java).apply {
                    action = "com.cs407.capstone.ACTION_TEST_NOTIFICATION"
                }
                context.startService(intent)
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Test Notification")
            }
        }
        item {
            Text(text = viewModel.predictionResult.value, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun RecentTransactionsScreen(viewModel: PredictViewModel) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(viewModel.recentTransactions) { transaction: Transaction ->
            TransactionItem(transaction)
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Amount: ${transaction.amt}")
            Text("Merchant: ${transaction.merchant}")
            Text("Category: ${transaction.category}")
        }
    }
}
