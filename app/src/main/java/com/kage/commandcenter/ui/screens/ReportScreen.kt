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
import com.kage.commandcenter.data.local.EventCategory
import com.kage.commandcenter.data.local.ProductivityLog
import com.kage.commandcenter.ui.theme.*

@Composable
fun ReportScreen(logs: List<ProductivityLog>) {
    val totalMinutes = logs.sumOf { it.actualMinutesCompleted }
    val jeeMinutes = logs.filter { it.category == EventCategory.JEE_STUDY }.sumOf { it.actualMinutesCompleted }
    val tradingMinutes = logs.filter { it.category == EventCategory.TRADING }.sumOf { it.actualMinutesCompleted }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "WEEKLY PRODUCTIVITY AUDIT",
                color = CyanAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Text(
                "Calculated based on completed and acknowledged sessions",
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
                    title = "JEE Prep Dedicated",
                    value = String.format("%.1f hrs", jeeMinutes / 60f),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            MetricSummaryCard(
                title = "Live Trading & Journaling",
                value = String.format("%.1f hrs", tradingMinutes / 60f),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                "SESSION LOG HISTORY",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        if (logs.isEmpty()) {
            item {
                Text(
                    "No sessions completed yet. Whenever you tap '✅ OK, DONE' on a reminder, the session log records here automatically.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
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