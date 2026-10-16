package com.kage.commandcenter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kage.commandcenter.data.local.EventCategory
import com.kage.commandcenter.data.local.Priority
import com.kage.commandcenter.data.local.ScheduleEvent
import com.kage.commandcenter.ui.theme.*
import com.kage.commandcenter.util.formatTimeRange

@Composable
fun TimetableScreen(
    events: List<ScheduleEvent>,
    onToggleEnable: (ScheduleEvent) -> Unit,
    onDeleteEvent: (ScheduleEvent) -> Unit,
    onAddEvent: (ScheduleEvent) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = CyanAccent,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Routine Event")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "DAILY ROUTINE MASTER",
                    color = CyanAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    "Toggle tasks active/inactive or create new ones",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            items(events) { event ->
                val categoryColor = when (event.category) {
                    EventCategory.DEEP_WORK -> CyanAccent
                    EventCategory.FITNESS -> VioletAccent
                    EventCategory.CHORES -> SuccessGreen
                    else -> TextSecondary
                }

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
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(if (event.isEnabled) categoryColor else TextSecondary.copy(alpha = 0.4f))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    event.title,
                                    color = if (event.isEnabled) TextPrimary else TextSecondary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                formatTimeRange(event.startHour, event.startMinute, event.endHour, event.endMinute),
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = event.isEnabled,
                                onCheckedChange = { onToggleEnable(event) }
                            )
                            IconButton(onClick = { onDeleteEvent(event) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CriticalRed.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showDialog) {
        AddEventDialog(
            onDismiss = { showDialog = false },
            onSave = { newEvent ->
                onAddEvent(newEvent)
                showDialog = false
            }
        )
    }
}

@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onSave: (ScheduleEvent) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var startHourStr by remember { mutableStateOf("9") }
    var startMinStr by remember { mutableStateOf("00") }
    var startIsPm by remember { mutableStateOf(false) }

    var endHourStr by remember { mutableStateOf("10") }
    var endMinStr by remember { mutableStateOf("00") }
    var endIsPm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Routine Item", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Start Time Row
                Text("Start Time", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = startHourStr,
                        onValueChange = { startHourStr = it },
                        label = { Text("Hour (1-12)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = startMinStr,
                        onValueChange = { startMinStr = it },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = { startIsPm = !startIsPm },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (startIsPm) VioletAccent else CyanAccent
                        )
                    ) {
                        Text(if (startIsPm) "PM" else "AM", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                // End Time Row
                Text("End Time", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = endHourStr,
                        onValueChange = { endHourStr = it },
                        label = { Text("Hour (1-12)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endMinStr,
                        onValueChange = { endMinStr = it },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = { endIsPm = !endIsPm },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (endIsPm) VioletAccent else CyanAccent
                        )
                    ) {
                        Text(if (endIsPm) "PM" else "AM", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rawStartHr = startHourStr.toIntOrNull() ?: 9
                    val sM = (startMinStr.toIntOrNull() ?: 0).coerceIn(0, 59)
                    val sH = when {
                        startIsPm && rawStartHr < 12 -> rawStartHr + 12
                        !startIsPm && rawStartHr == 12 -> 0
                        else -> rawStartHr.coerceIn(0, 23)
                    }

                    val rawEndHr = endHourStr.toIntOrNull() ?: 10
                    val eM = (endMinStr.toIntOrNull() ?: 0).coerceIn(0, 59)
                    val eH = when {
                        endIsPm && rawEndHr < 12 -> rawEndHr + 12
                        !endIsPm && rawEndHr == 12 -> 0
                        else -> rawEndHr.coerceIn(0, 23)
                    }

                    if (title.isNotBlank()) {
                        onSave(
                            ScheduleEvent(
                                title = title,
                                category = EventCategory.ROUTINE,
                                priority = Priority.HIGH,
                                startHour = sH,
                                startMinute = sM,
                                endHour = eH,
                                endMinute = eM,
                                daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7)
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Save", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = CharcoalSurface
    )
}