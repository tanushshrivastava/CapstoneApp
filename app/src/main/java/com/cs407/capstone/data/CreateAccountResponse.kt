package com.cs407.capstone.data

data class CreateAccountResponse(
    val accountId: String,
    val fraudThreshold: Double,
    val phoneNumber: String?
)
