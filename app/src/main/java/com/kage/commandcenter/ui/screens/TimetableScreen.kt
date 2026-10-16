package com.kage.commandcenter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
                    EventCategory.JEE_STUDY -> CyanAccent
                    EventCategory.TRADING -> VioletAccent
                    EventCategory.GAMING -> SuccessGreen
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
                                String.format("%02d:%02d – %02d:%02d | %s", event.startHour, event.startMinute, event.endHour, event.endMinute, event.category.name),
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
    var startHour by remember { mutableStateOf("09") }
    var startMin by remember { mutableStateOf("00") }
    var endHour by remember { mutableStateOf("10") }
    var endMin by remember { mutableStateOf("00") }
    var selectedCategory by remember { mutableStateOf(EventCategory.JEE_STUDY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Routine Item", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startHour,
                        onValueChange = { startHour = it },
                        label = { Text("Start Hr (0-23)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = startMin,
                        onValueChange = { startMin = it },
                        label = { Text("Start Min") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = endHour,
                        onValueChange = { endHour = it },
                        label = { Text("End Hr (0-23)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endMin,
                        onValueChange = { endMin = it },
                        label = { Text("End Min") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sH = startHour.toIntOrNull() ?: 9
                    val sM = startMin.toIntOrNull() ?: 0
                    val eH = endHour.toIntOrNull() ?: 10
                    val eM = endMin.toIntOrNull() ?: 0
                    if (title.isNotBlank()) {
                        onSave(
                            ScheduleEvent(
                                title = title,
                                category = selectedCategory,
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