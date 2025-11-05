package com.cs407.capstone.api

import com.google.gson.annotations.SerializedName

data class PostTransactionRequest(
    val accountId: String,
    val transaction: Transaction
)

// Matches the JSON structure you provided
data class Transaction(
    @SerializedName("trans_date_trans_time") val transDateTime: String,
    val merchant: String,
    val category: String,
    val amt: Double,
    @SerializedName("unix_time") val unixTime: Long,
    val gender: String,
    val state: String,
    val job: String,
    val dob: String,
    val lat: Double,
    val long: Double,
    @SerializedName("city_pop") val cityPop: Int,
    @SerializedName("merch_lat") val merchLat: Double,
    @SerializedName("merch_long") val merchLong: Double
)

data class PostTransactionResponse(
    val score: Double,
    val payload: Transaction // Assuming the payload in the response is the same as the one sent
)
