package com.kage.commandcenter

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kage.commandcenter.data.local.AppDatabase
import com.kage.commandcenter.engine.KageAlarmScheduler
import com.kage.commandcenter.receiver.NotificationActionReceiver
import com.kage.commandcenter.ui.screens.DashboardScreen
import com.kage.commandcenter.ui.screens.ReportScreen
import com.kage.commandcenter.ui.screens.SettingsScreen
import com.kage.commandcenter.ui.screens.TimetableScreen
import com.kage.commandcenter.ui.theme.CharcoalBackground
import com.kage.commandcenter.ui.theme.KageTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val db by lazy { AppDatabase.getDatabase(applicationContext, lifecycleScope) }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkAndScheduleInitialAlarms()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkNotificationAndAlarmPermissions()

        setContent {
            KageTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

                val events by db.scheduleDao().getAllEvents().collectAsState(initial = emptyList())
                val activeReminders by db.reminderDao().getActiveReminders().collectAsState(initial = emptyList())
                val logs by db.productivityDao().getAllLogs().collectAsState(initial = emptyList())

                Scaffold(
                    containerColor = CharcoalBackground,
                    bottomBar = {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Command") },
                                selected = currentRoute == "dashboard",
                                onClick = { navController.navigate("dashboard") { launchSingleTop = true } }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.List, contentDescription = "Routine") },
                                label = { Text("Routine") },
                                selected = currentRoute == "timetable",
                                onClick = { navController.navigate("timetable") { launchSingleTop = true } }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.BarChart, contentDescription = "Report") },
                                label = { Text("Report") },
                                selected = currentRoute == "report",
                                onClick = { navController.navigate("report") { launchSingleTop = true } }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Setup") },
                                selected = currentRoute == "settings",
                                onClick = { navController.navigate("settings") { launchSingleTop = true } }
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
                                }
                            )
                        }

                        composable("timetable") {
                            TimetableScreen(
                                events = events,
                                onToggleEnable = { event ->
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        val updated = event.copy(isEnabled = !event.isEnabled)
                                        db.scheduleDao().updateEvent(updated)
                                        if (updated.isEnabled) {
                                            KageAlarmScheduler.scheduleEvent(applicationContext, updated)
                                        } else {
                                            KageAlarmScheduler.cancelEvent(applicationContext, updated.id)
                                        }
                                    }
                                },
                                onDeleteEvent = { event ->
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        db.scheduleDao().deleteEvent(event)
                                        KageAlarmScheduler.cancelEvent(applicationContext, event.id)
                                    }
                                },
                                onAddEvent = { newEvent ->
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        val id = db.scheduleDao().insertEvent(newEvent)
                                        KageAlarmScheduler.scheduleEvent(applicationContext, newEvent.copy(id = id))
                                    }
                                }
                            )
                        }

                        composable("report") {
                            ReportScreen(logs = logs)
                        }

                        composable("settings") {
                            SettingsScreen()
                        }
                    }
                }
            }
        }
    }

    private fun checkNotificationAndAlarmPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                checkAndScheduleInitialAlarms()
            }
        } else {
            checkAndScheduleInitialAlarms()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

    private fun checkAndScheduleInitialAlarms() {
        lifecycleScope.launch(Dispatchers.IO) {
            val events = db.scheduleDao().getActiveEventsSync()
            for (event in events) {
                KageAlarmScheduler.scheduleEvent(applicationContext, event)
            }
        }
    }
}