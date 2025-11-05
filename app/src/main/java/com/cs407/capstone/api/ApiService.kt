package com.cs407.capstone.api

import com.cs407.capstone.data.CreateAccountRequest
import com.cs407.capstone.data.CreateAccountResponse
import com.cs407.capstone.data.GetRecentTransactionsResponse
import com.cs407.capstone.data.LoginRequest
import com.cs407.capstone.data.LoginResponse
import com.cs407.capstone.data.Transaction
import com.cs407.capstone.data.TransactionRequest
import com.cs407.capstone.data.UpdateAccountSettingsRequest
import com.cs407.capstone.data.UpdateAccountSettingsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("prod/accounts")
    suspend fun createAccount(@Body request: CreateAccountRequest): Response<CreateAccountResponse>

    @POST("prod/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @PATCH("prod/accounts/settings")
    suspend fun updateAccountSettings(@Body request: UpdateAccountSettingsRequest): Response<UpdateAccountSettingsResponse>

    @GET("prod/accounts/{accountId}/transactions")
    suspend fun getRecentTransactions(@Path("accountId") accountId: String): Response<GetRecentTransactionsResponse>

    @POST("prod/transactions")
    suspend fun postTransaction(@Body post: TransactionRequest): Response<Transaction>
}