package com.cs407.capstone.data

data class CreateAccountRequest(
    val username: String,
    val email: String,
    val password: String,
    val first_name: String,
    val last_name: String,
    val cc_num: String,
    val gender: String,
    val date_of_birth: String,
    val job: String,
    val street: String,
    val city: String,
    val state: String,
    val zip: String,
    val address: String,
    val phoneNumber: String,
    val fraudThreshold: Double,
    val smsOptIn: Boolean
)
