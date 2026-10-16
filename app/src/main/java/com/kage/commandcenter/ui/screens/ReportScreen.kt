package com.kage.commandcenter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kage.commandcenter.data.local.ProductivityLog
import com.kage.commandcenter.ui.theme.*

@Composable
fun ReportScreen(logs: List<ProductivityLog>) {
    val totalMinutes = logs.sumOf { it.actualMinutesCompleted }
    val onTimeSessions = logs.count { it.wasAcknowledgedOnTime }
    val adherenceRate = if (logs.isNotEmpty()) (onTimeSessions * 100) / logs.size else 100

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "ROUTINE AUDIT",
                color = CyanAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Text(
                "Completed tasks and adherence metrics",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricSummaryCard(
                    title = "Total Time Logged",
                    value = String.format("%.1f hrs", totalMinutes / 60f),
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "Adherence Rate",
                    value = "$adherenceRate%",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                "COMPLETED SESSIONS",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "No sessions completed yet.",
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Whenever you tap 'OK, DONE' on a reminder, your session will be logged here.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(logs) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            log.category.name.replace("_", " "),
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${log.actualMinutesCompleted} mins",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(64.dp)) }
    }
}

@Composable
fun MetricSummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}