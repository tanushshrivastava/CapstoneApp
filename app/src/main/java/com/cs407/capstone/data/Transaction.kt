package com.cs407.capstone.data

data class TransactionRequest(
    val accountId: String,
    val transaction: Transaction
)

data class Transaction(
    val trans_date_trans_time: String,
    val cc_num: String,
    val amt: Double,
    val category: String,
    val merchant: String,
    val first: String,
    val last: String,
    val gender: String,
    val dob: String,
    val job: String,
    val street: String,
    val city: String,
    val state: String,
    val zip: String,
    val lat: Double,
    val long: Double,
    val city_pop: Int,
    val merch_lat: Double,
    val merch_long: Double,
    val trans_num: String,
    val unix_time: Long,
    val prediction: Prediction? = null,
    val fraudScore: Double? = null,
    val fraudThreshold: Double? = null
)
