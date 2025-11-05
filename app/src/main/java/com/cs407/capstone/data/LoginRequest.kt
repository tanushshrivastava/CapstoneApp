package com.cs407.capstone.data

data class LoginRequest(
    val accountId: String? = null,
    val username: String? = null,
    val password: String
)
