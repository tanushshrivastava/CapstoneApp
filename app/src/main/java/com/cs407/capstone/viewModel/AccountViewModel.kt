/**
 * AccountViewModel.kt
 * 
 * Manages user account state and operations:
 * - User authentication (login/logout)
 * - Account creation with comprehensive user data
 * - Settings management (phone number, fraud threshold)
 * - Persistent login using SharedPreferences
 * - API communication for account operations
 * 
 * Account Creation Fields:
 * - Basic: username, email, password
 * - Personal: first_name, last_name, gender, date_of_birth, job
 * - Financial: cc_num, fraudThreshold, smsOptIn
 * - Location: street, city, state, zip, address
 * - Contact: phoneNumber
 * 
 * Backend auto-populates: cityPopulation, lat/long, accountId, timestamps
 */
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
    var password by mutableStateOf("")
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var ccNum by mutableStateOf("")
    var gender by mutableStateOf("")
    var dateOfBirth by mutableStateOf("")
    var job by mutableStateOf("")
    var street by mutableStateOf("")
    var city by mutableStateOf("")
    var state by mutableStateOf("")
    var zip by mutableStateOf("")
    var address by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var smsOptIn by mutableStateOf(true)

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
        if (username.isBlank() || email.isBlank() || password.isBlank() || 
            firstName.isBlank() || lastName.isBlank() || ccNum.isBlank() ||
            phoneNumber.isBlank()) {
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

    /**
     * Create new user account with comprehensive user data
     * Sends all user fields to backend, which auto-populates:
     * - cityPopulation (from zip code)
     * - latitude/longitude (from address geocoding)
     * - accountId, passwordHash, createdAt (system generated)
     */
    fun createAccount() {
        if (!validateCreateAccountFields()) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            // Create comprehensive account request with all user data
            val request = CreateAccountRequest(
                username = username,
                email = email,
                password = password,
                first_name = firstName,
                last_name = lastName,
                cc_num = ccNum,
                gender = gender,
                date_of_birth = dateOfBirth,
                job = job,
                street = street,
                city = city,
                state = state,
                zip = zip,
                address = address,
                phoneNumber = phoneNumber,
                fraudThreshold = fraudThreshold,
                smsOptIn = smsOptIn
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
            } finally {
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

    /**
     * Save account data to SharedPreferences for persistent login
     * Stores: accountId, username, phone, fraudThreshold, login status
     */
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
    
    /**
     * Load saved account data on app startup
     * Restores login state if user was previously logged in
     */
    private fun loadSavedAccount() {
        val savedAccountId = sharedPreferences.getString("account_id", null)
        val savedUsername = sharedPreferences.getString("username", null)
        val savedPhoneNumber = sharedPreferences.getString("phone_number", "")
        val savedFraudThreshold = sharedPreferences.getFloat("fraud_threshold", 0.0f).toDouble()
        val savedIsLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        
        // Restore login state if all required data is present
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
        password = ""
        firstName = ""
        lastName = ""
        ccNum = ""
        gender = ""
        dateOfBirth = ""
        job = ""
        street = ""
        city = ""
        state = ""
        zip = ""
        address = ""
        phoneNumber = ""
        fraudThreshold = 0.0
        smsOptIn = true
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
