package com.kage.commandcenter.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kage.commandcenter.ui.theme.*

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "SYSTEM RELIABILITY & POWER",
                color = CyanAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Text(
                "Configure Android background execution to ensure 30s repeats never freeze",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. Ignore Battery Optimizations", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text(
                        "Stops Android from putting the reminder loop to sleep when your screen is off.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                    ) {
                        Text("Disable Battery Restrictions", color = CharcoalBackground)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("2. Exact Alarms Permission", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text(
                        "Required on Android 12+ to trigger reminders at the exact scheduled second.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletAccent)
                    ) {
                        Text("Open Exact Alarm Settings", color = TextPrimary)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("3. Xiaomi / HyperOS / OEM Setup", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text(
                        "On MIUI/HyperOS, Realme, and ColorOS:\n" +
                        "• Long-press the KAGE icon on your home screen\n" +
                        "• Tap App Info → Turn 'Autostart' ON\n" +
                        "• Tap Battery Saver → Choose 'No restrictions'",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(64.dp)) }
    }
}