package com.cs407.capstone.data

data class GetRecentTransactionsResponse(
    val accountId: String,
    val items: List<Transaction>
)
