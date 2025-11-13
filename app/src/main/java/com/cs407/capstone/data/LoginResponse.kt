package com.cs407.capstone.data

data class LoginResponse(
    val accountId: String,
    val username: String,
    val phoneNumber: String?,
    val fraudThreshold: Double,
    val recentTransactions: List<RecentTransaction>,
    val message: String
)

data class RecentTransaction(
    val transactionId: String,
    val createdAt: String,
    val fraudScore: Double,
    val amount: String,
    val awaitingResponse: Boolean,
    val customerResponseFlag: Int,
    val phoneNumber: String,
    val city: String,
    val merchant: String,
    val category: String,
    val customerMarkedFraud: Boolean,
    val prediction: Prediction,
    val customerResponseText: String? = null,
    val customerRespondedAt: String? = null
)
