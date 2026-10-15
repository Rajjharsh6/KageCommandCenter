package com.kage.commandcenter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kage.commandcenter.data.local.AppDatabase
import com.kage.commandcenter.receiver.NotificationActionReceiver
import com.kage.commandcenter.ui.screens.DashboardScreen
import com.kage.commandcenter.ui.theme.CharcoalBackground
import com.kage.commandcenter.ui.theme.KageTheme

class MainActivity : ComponentActivity() {

    private val db by lazy { AppDatabase.getDatabase(applicationContext, lifecycleScope) }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()

        setContent {
            KageTheme {
                val navController = rememberNavController()
                val events by db.scheduleDao().getAllEvents().collectAsState(initial = emptyList())
                val activeReminders by db.reminderDao().getActiveReminders().collectAsState(initial = emptyList())

                Scaffold(
                    containerColor = CharcoalBackground,
                    bottomBar = {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Command") },
                                selected = true,
                                onClick = { navController.navigate("dashboard") }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.List, contentDescription = "Schedule") },
                                label = { Text("Routine") },
                                selected = false,
                                onClick = { }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") },
                                label = { Text("Report") },
                                selected = false,
                                onClick = { }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Setup") },
                                selected = false,
                                onClick = { }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("dashboard") {
                            DashboardScreen(
                                events = events,
                                activeReminders = activeReminders,
                                onAcknowledge = { instanceId ->
                                    val intent = Intent(this@MainActivity, NotificationActionReceiver::class.java).apply {
                                        action = "com.kage.ACTION_ACKNOWLEDGE"
                                        putExtra("EXTRA_INSTANCE_ID", instanceId)
                                    }
                                    sendBroadcast(intent)
                                },
                                onSnooze = { instanceId, minutes ->
                                    val intent = Intent(this@MainActivity, NotificationActionReceiver::class.java).apply {
                                        action = "com.kage.ACTION_SNOOZE"
                                        putExtra("EXTRA_INSTANCE_ID", instanceId)
                                        putExtra("EXTRA_SNOOZE_MINUTES", minutes)
                                    }
                                    sendBroadcast(intent)
                                },
                                onQuickAdd = { }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}