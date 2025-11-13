
package com.cs407.capstone.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.capstone.api.RetrofitClient
import com.cs407.capstone.data.Transaction
import com.cs407.capstone.data.TransactionRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PredictViewModel : ViewModel() {
    val transactionDate = mutableStateOf("")
    val ccNum = mutableStateOf("")
    val amount = mutableStateOf("")
    val category = mutableStateOf("")
    val merchant = mutableStateOf("")
    val firstName = mutableStateOf("")
    val lastName = mutableStateOf("")
    val gender = mutableStateOf("")
    val dob = mutableStateOf("")
    val job = mutableStateOf("")
    val street = mutableStateOf("")
    val city = mutableStateOf("")
    val state = mutableStateOf("")
    val zip = mutableStateOf("")
    val lat = mutableStateOf("")
    val long = mutableStateOf("")
    val cityPop = mutableStateOf("")
    val merchLat = mutableStateOf("")
    val merchLong = mutableStateOf("")
    val transNum = mutableStateOf("")
    val unixTime = mutableStateOf("")
    val predictionResult = mutableStateOf("")

    val recentTransactions = mutableStateListOf<com.cs407.capstone.data.RecentTransaction>()

    // Hardcoded templates
    private val templates = listOf(
        Transaction(
            trans_date_trans_time = "2020-06-21 22:37:27",
            cc_num = "6564459919350820",
            amt = 62000.32,
            category = "entertainment",
            merchant = "fraud_Nienow PLC",
            first = "Douglas",
            last = "Willis",
            gender = "M",
            dob = "1958-09-10",
            job = "Public relations officer",
            street = "619 Jeremy Garden Apt. 681",
            city = "Benton",
            state = "WI",
            zip = "53803",
            lat = 42.5545,
            long = -90.3508,
            city_pop = 1306,
            merch_lat = 42.771834000000005,
            merch_long = -90.158365,
            trans_num = "47a9987ae81d99f7832a54b29a77bf4b",
            unix_time = 1371854247
        )
        // Add more templates as needed
    )

    fun getTemplates(): List<Transaction> {
        return templates
    }

    fun loadTemplate(template: Transaction) {
        transactionDate.value = template.trans_date_trans_time
        ccNum.value = template.cc_num
        amount.value = template.amt.toString()
        category.value = template.category
        merchant.value = template.merchant
        firstName.value = template.first
        lastName.value = template.last
        gender.value = template.gender
        dob.value = template.dob
        job.value = template.job
        street.value = template.street
        city.value = template.city
        state.value = template.state
        zip.value = template.zip
        lat.value = template.lat.toString()
        long.value = template.long.toString()
        cityPop.value = template.city_pop.toString()
        merchLat.value = template.merch_lat.toString()
        merchLong.value = template.merch_long.toString()
        transNum.value = template.trans_num
        unixTime.value = template.unix_time.toString()
    }

    fun getRecentTransactions(accountId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getRecentTransactions(accountId)
                if (response.isSuccessful) {
                    recentTransactions.clear()
                    response.body()?.items?.let { recentTransactions.addAll(it) }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun sendPredictionRequest(accountId: String) {
        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    trans_date_trans_time = transactionDate.value,
                    cc_num = ccNum.value,
                    amt = amount.value.toDouble(),
                    category = category.value,
                    merchant = merchant.value,
                    first = firstName.value,
                    last = lastName.value,
                    gender = gender.value,
                    dob = dob.value,
                    job = job.value,
                    street = street.value,
                    city = city.value,
                    state = state.value,
                    zip = zip.value,
                    lat = lat.value.toDouble(),
                    long = long.value.toDouble(),
                    city_pop = cityPop.value.toInt(),
                    merch_lat = merchLat.value.toDouble(),
                    merch_long = merchLong.value.toDouble(),
                    trans_num = transNum.value,
                    unix_time = unixTime.value.toLong()
                )
                val post = TransactionRequest(
                    accountId = accountId,
                    transaction = transaction
                )

                val response = RetrofitClient.apiService.postTransaction(post)
                val responseBody = response.body()
                predictionResult.value = if (responseBody != null) {
                    val fraudScore = responseBody.fraudScore
                    val fraudThreshold = responseBody.fraudThreshold
                    "Fraud Score: $fraudScore, Fraud Threshold: $fraudThreshold"
                } else {
                    "No prediction available"
                }
            } catch (e: Exception) {
                predictionResult.value = "Error: ${e.message}"
            }
        }
    }
}
