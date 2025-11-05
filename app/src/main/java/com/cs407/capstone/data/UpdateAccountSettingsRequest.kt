package com.cs407.capstone.data

data class UpdateAccountSettingsRequest(
    val accountId: String,
    val phoneNumber: String?,
    val fraudThreshold: Double?
)
