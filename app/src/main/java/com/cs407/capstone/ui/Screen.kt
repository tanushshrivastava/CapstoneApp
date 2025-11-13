package com.cs407.capstone.ui

sealed class Screen(val route: String, val title: String) {
    object Predict : Screen("predict", "Transactions")
    object Account : Screen("account", "Account")
    object About : Screen("about", "About")
    object Notifications : Screen("notifications", "Notifications")
}
