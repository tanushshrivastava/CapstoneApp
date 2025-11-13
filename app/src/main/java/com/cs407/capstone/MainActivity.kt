package com.cs407.capstone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cs407.capstone.ui.Screen
import com.cs407.capstone.ui.screens.AboutScreen
import com.cs407.capstone.ui.screens.AccountScreen
import com.cs407.capstone.ui.screens.NotificationScreen
import com.cs407.capstone.ui.screens.PredictScreen
import com.cs407.capstone.ui.theme.CapstoneTheme
import com.cs407.capstone.ui.components.TopBar
import com.cs407.capstone.viewModel.AccountViewModel
import com.cs407.capstone.viewModel.Notification
import com.cs407.capstone.viewModel.NotificationViewModel
import com.cs407.capstone.viewModel.PredictViewModel

class MainActivity : ComponentActivity() {
    private val accountViewModel: AccountViewModel by lazy { AccountViewModel(this) }
    private val predictViewModel: PredictViewModel by viewModels()
    private val notificationViewModel: NotificationViewModel by viewModels()

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val title = intent.getStringExtra("title") ?: ""
            val text = intent.getStringExtra("text") ?: ""
            notificationViewModel.addNotification(Notification(title, text))
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(this).registerReceiver(
            notificationReceiver,
            IntentFilter("com.cs407.capstone.NOTIFICATION_LISTENER")
        )
        setContent {
            CapstoneTheme {
                val navController = rememberNavController()
                val items = listOf(
                    Screen.Account,
                    Screen.About
                )



                Scaffold(
                    topBar = { TopBar() },
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(getIconForScreen(screen), contentDescription = null) },
                                    label = { Text(screen.title) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = Screen.Account.route,
                        Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Account.route) { AccountScreen(accountViewModel = accountViewModel) }
                        composable(Screen.About.route) { AboutScreen() }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver)
    }
}

fun getIconForScreen(screen: Screen): ImageVector {
    return when (screen) {
        Screen.Account -> Icons.Default.AccountCircle
        Screen.About -> Icons.Default.Info
        else -> Icons.Default.Info
    }
}
