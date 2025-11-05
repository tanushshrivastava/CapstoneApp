package com.cs407.capstone.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.capstone.api.ApiService
import com.cs407.capstone.data.CreateAccountRequest
import com.cs407.capstone.data.LoginRequest
import com.cs407.capstone.api.RetrofitClient
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {
    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var address by mutableStateOf("")
    var password by mutableStateOf("")
    var phoneNumber by mutableStateOf("")

    var identifier by mutableStateOf("")
    var loginPassword by mutableStateOf("")

    var isLoggedIn by mutableStateOf(false)
    var loggedInAccount by mutableStateOf<Account?>(null)

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val apiService: ApiService = RetrofitClient.apiService

    private fun validateCreateAccountFields(): Boolean {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage = "Please fill in all required fields."
            return false
        }
        return true
    }

    private fun validateLoginFields(): Boolean {
        if (identifier.isBlank() || loginPassword.isBlank()) {
            errorMessage = "Please enter your username/email and password."
            return false
        }
        return true
    }

    fun createAccount() {
        if (!validateCreateAccountFields()) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val request = CreateAccountRequest(
                username = username,
                email = email,
                password = password,
                phoneNumber = phoneNumber,
                address = address
            )
            try {
                val response = apiService.createAccount(request)
                if (response.isSuccessful) {
                    val accountResponse = response.body()!!
                    loggedInAccount = Account(accountResponse.accountId, username, email)
                    isLoggedIn = true
                } else {
                    errorMessage = "Error creating account. Please try again."
                }
            } catch (e: Exception) {
                errorMessage = "An unexpected error occurred. Please check your network connection."
            }
            finally {
                isLoading = false
            }
        }
    }

    fun login() {
        if (!validateLoginFields()) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val request = LoginRequest(username = identifier, password = loginPassword)
            try {
                val response = apiService.login(request)
                if (response.isSuccessful) {
                    val accountResponse = response.body()!!
                    loggedInAccount = Account(accountResponse.accountId, accountResponse.username, accountResponse.email)
                    isLoggedIn = true
                } else {
                    errorMessage = "Invalid credentials. Please try again."
                }
            } catch (e: Exception) {
                errorMessage = e.toString()
            }
            finally {
                isLoading = false
            }
        }
    }

    fun logout() {
        isLoggedIn = false
        loggedInAccount = null
        username = ""
        email = ""
        address = ""
        password = ""
        phoneNumber = ""
        identifier = ""
        loginPassword = ""
        errorMessage = null
    }
}

data class Account(val accountId: String, val username: String, val email: String?)
