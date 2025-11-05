package com.cs407.capstone.data

data class LoginResponse(
    val accountId: String,
    val username: String,
    val email: String,
    val address: String,
    val phoneNumber: String?,
    val fraudThreshold: Double,
    val recentTransactions: List<TransactionRequest>
)
