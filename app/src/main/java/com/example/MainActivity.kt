package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.service.SmsGatewayForegroundService
import com.example.ui.SmsGatewayViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Campaigns : Screen("campaigns", "Campaigns", Icons.Default.Campaign)
    object Queue : Screen("queue", "Queue", Icons.Default.HourglassEmpty)
    object History : Screen("history", "History", Icons.Default.History)
    object Failed : Screen("failed", "Failed", Icons.Default.Error)
    object Retry : Screen("retry", "Retry", Icons.Default.Refresh)
    object Reports : Screen("reports", "Reports", Icons.Default.BarChart)
    object Sim : Screen("sim", "SIM", Icons.Default.SimCard)
    object Config : Screen("config", "Server", Icons.Default.Cloud)
    object DeviceInfo : Screen("device_info", "Device", Icons.Default.PhoneAndroid)
    object Logs : Screen("logs", "Logs", Icons.Default.List)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permissions
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkAndRequestPermissions()

        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_PHONE_NUMBERS
        )
        val missing = permissions.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing) {
            permissionLauncher.launch(permissions)
        }
    }

    private fun startForegroundService() {
        val intent = Intent(this, SmsGatewayForegroundService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val viewModel: SmsGatewayViewModel = viewModel()

    val screens = listOf(
        Screen.Dashboard,
        Screen.Campaigns,
        Screen.Queue,
        Screen.History,
        Screen.Failed,
        Screen.Retry,
        Screen.Reports,
        Screen.Sim,
        Screen.Config,
        Screen.DeviceInfo,
        Screen.Logs,
        Screen.Settings
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Professional SMS Gateway") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Show primary items in bottom bar (Dashboard, Queue, Failed, Config, Settings)
            NavigationBar {
                val bottomScreens = listOf(Screen.Dashboard, Screen.Queue, Screen.Failed, Screen.Config, Screen.Settings)
                bottomScreens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
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
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(viewModel) { route -> navController.navigate(route) } }
            composable(Screen.Campaigns.route) { CampaignsScreen(viewModel) }
            composable(Screen.Queue.route) { QueueScreen(viewModel) }
            composable(Screen.History.route) { HistoryScreen(viewModel) }
            composable(Screen.Failed.route) { FailedScreen(viewModel) }
            composable(Screen.Retry.route) { RetryScreen(viewModel) }
            composable(Screen.Reports.route) { ReportsScreen(viewModel) }
            composable(Screen.Sim.route) { SimScreen(viewModel) }
            composable(Screen.Config.route) { ConfigScreen(viewModel) }
            composable(Screen.DeviceInfo.route) { DeviceInfoScreen(viewModel) }
            composable(Screen.Logs.route) { LogsScreen(viewModel) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel) }
        }
    }
}
