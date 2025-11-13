package com.cs407.capstone.viewModel

import android.content.Context
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

class AccountViewModel(private val context: Context) : ViewModel() {
    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var address by mutableStateOf("")
    var password by mutableStateOf("")
    var phoneNumber by mutableStateOf("")

    var identifier by mutableStateOf("")
    var loginPassword by mutableStateOf("")

    var isLoggedIn by mutableStateOf(false)
    var loggedInAccount by mutableStateOf<Account?>(null)
    var fraudThreshold by mutableStateOf(0.0)

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val apiService: ApiService = RetrofitClient.apiService
    private val sharedPreferences = context.getSharedPreferences("account_prefs", Context.MODE_PRIVATE)
    
    init {
        loadSavedAccount()
    }

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
                    loggedInAccount = Account(accountResponse.accountId, accountResponse.username, null)
                    phoneNumber = accountResponse.phoneNumber ?: ""
                    fraudThreshold = accountResponse.fraudThreshold
                    isLoggedIn = true
                    saveAccount(accountResponse.accountId, accountResponse.username, phoneNumber, fraudThreshold)
                } else {
                    errorMessage = "Invalid credentials. Please try again."
                }
            } catch (e: Exception) {
                errorMessage = "Network error. Please check your connection and try again."
            }
            finally {
                isLoading = false
            }
        }
    }

    fun updateAccountSettings() {
        val accountId = loggedInAccount?.accountId ?: return
        
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val request = com.cs407.capstone.data.UpdateAccountSettingsRequest(
                accountId = accountId,
                phoneNumber = phoneNumber,
                fraudThreshold = fraudThreshold
            )
            try {
                val response = apiService.updateAccountSettings(request)
                if (response.isSuccessful) {
                    errorMessage = "✅ Settings updated successfully!"
                    saveAccount(accountId, loggedInAccount?.username ?: "", phoneNumber, fraudThreshold)
                } else {
                    errorMessage = "❌ Failed to update settings"
                }
            } catch (e: Exception) {
                errorMessage = "❌ Network error. Please try again."
            } finally {
                isLoading = false
            }
        }
    }

    private fun saveAccount(accountId: String, username: String, phoneNumber: String, fraudThreshold: Double) {
        with(sharedPreferences.edit()) {
            putString("account_id", accountId)
            putString("username", username)
            putString("phone_number", phoneNumber)
            putFloat("fraud_threshold", fraudThreshold.toFloat())
            putBoolean("is_logged_in", true)
            apply()
        }
    }
    
    private fun loadSavedAccount() {
        val savedAccountId = sharedPreferences.getString("account_id", null)
        val savedUsername = sharedPreferences.getString("username", null)
        val savedPhoneNumber = sharedPreferences.getString("phone_number", "")
        val savedFraudThreshold = sharedPreferences.getFloat("fraud_threshold", 0.0f).toDouble()
        val savedIsLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        
        if (savedIsLoggedIn && savedAccountId != null && savedUsername != null) {
            loggedInAccount = Account(savedAccountId, savedUsername, null)
            phoneNumber = savedPhoneNumber ?: ""
            fraudThreshold = savedFraudThreshold
            isLoggedIn = true
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
        fraudThreshold = 0.0
        identifier = ""
        loginPassword = ""
        errorMessage = null
        
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }
}

data class Account(val accountId: String, val username: String, val email: String?)
