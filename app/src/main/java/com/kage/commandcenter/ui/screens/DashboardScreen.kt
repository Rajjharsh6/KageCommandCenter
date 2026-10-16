package com.kage.commandcenter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kage.commandcenter.data.local.ReminderInstance
import com.kage.commandcenter.data.local.ScheduleEvent
import com.kage.commandcenter.ui.theme.*
import com.kage.commandcenter.util.formatTimeRange
import java.time.LocalTime

@Composable
fun DashboardScreen(
    events: List<ScheduleEvent>,
    activeReminders: List<ReminderInstance>,
    onAcknowledge: (Long) -> Unit,
    onSnooze: (Long, Int) -> Unit
) {
    val activeEventsCount = events.count { it.isEnabled }
    val now = LocalTime.now()
    val nowMinutes = now.hour * 60 + now.minute

    val currentTask = events.firstOrNull { event ->
        if (!event.isEnabled) return@firstOrNull false
        val start = event.startHour * 60 + event.startMinute
        val end = event.endHour * 60 + event.endMinute
        if (end > start) nowMinutes in start until end
        else nowMinutes >= start || nowMinutes < end
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    "SCHEDULE REMINDER",
                    color = CyanAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )
                Text(
                    "Live routine monitor & persistent alerts",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        if (activeReminders.isNotEmpty()) {
            item {
                val reminder = activeReminders.first()
                Card(
                    colors = CardDefaults.cardColors(containerColor = CriticalRed.copy(alpha = 0.18f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(CriticalRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "ACTION REQUIRED (RINGS EVERY 30S)",
                                color = CriticalRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            reminder.eventTitle,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { onAcknowledge(reminder.instanceId) },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("✅ OK, DONE", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { onSnooze(reminder.instanceId, 10) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("⏳ SNOOZE 10M", color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Active Tasks", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$activeEventsCount Active", color = CyanAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Current Slot", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            currentTask?.title ?: "Free / Idle",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        item {
            Text(
                "SCHEDULED ROUTINE",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        if (events.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "No tasks configured yet.",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Switch to the Routine tab below to add your daily time slots.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(events) { event ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                event.title,
                                color = if (event.isEnabled) TextPrimary else TextSecondary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                formatTimeRange(event.startHour, event.startMinute, event.endHour, event.endMinute),
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            if (event.isEnabled) "ACTIVE" else "OFF",
                            color = if (event.isEnabled) SuccessGreen else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}