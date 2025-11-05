package com.cs407.capstone.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.capstone.viewModel.AccountViewModel

@Composable
fun AccountScreen(accountViewModel: AccountViewModel = viewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (accountViewModel.isLoggedIn) {
            LoggedInSection(accountViewModel)
        } else {
            LoggedOutSection(accountViewModel)
        }
    }
}

@Composable
fun LoggedInSection(accountViewModel: AccountViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Account Details", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Username: ${accountViewModel.loggedInAccount?.username}")
            Text("Email: ${accountViewModel.loggedInAccount?.email}")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { accountViewModel.logout() }) {
                Text("Sign Out")
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
            Text(it, color = Color.Red)
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
