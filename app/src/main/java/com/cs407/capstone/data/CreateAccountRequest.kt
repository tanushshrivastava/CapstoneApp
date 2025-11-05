package com.cs407.capstone.data

data class CreateAccountRequest(
    val username: String,
    val email: String,
    val address: String,
    val password: String,
    val phoneNumber: String? = null,
    val fraudThreshold: Double? = null
)
