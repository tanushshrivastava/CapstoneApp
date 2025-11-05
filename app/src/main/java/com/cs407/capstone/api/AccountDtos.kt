package com.cs407.capstone.api

data class CreateAccountRequest(
    val username: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val address: String
)

data class CreateAccountResponse(
    val accountId: String,
    val username: String,
    val email: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val accountId: String,
    val username: String,
    val email: String
)
