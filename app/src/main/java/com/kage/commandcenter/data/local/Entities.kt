package com.kage.commandcenter.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EventCategory {
    JEE_STUDY,
    TRADING,
    GAMING,
    BREAK,
    SLEEP,
    REVIEW,
    GENERAL
}

enum class Priority {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class ReminderStatus {
    SCHEDULED,
    ACTIVE_UNACKNOWLEDGED,
    ACKNOWLEDGED,
    SNOOZED,
    MISSED
}

@Entity(tableName = "events")
data class ScheduleEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: EventCategory,
    val priority: Priority,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: List<Int>,
    val isEnabled: Boolean = true,
    val allowOverlap: Boolean = false,
    val prepReminderMinutes: Int = 0
)

@Entity(tableName = "reminder_instances")
data class ReminderInstance(
    @PrimaryKey(autoGenerate = true) val instanceId: Long = 0,
    val eventId: Long,
    val eventTitle: String,
    val category: EventCategory,
    val scheduledEpochMillis: Long,
    val status: ReminderStatus,
    val repeatCount: Int = 0,
    val acknowledgedAtEpochMillis: Long? = null,
    val snoozedUntilEpochMillis: Long? = null
)

@Entity(tableName = "productivity_logs")
data class ProductivityLog(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0,
    val eventId: Long,
    val category: EventCategory,
    val dateEpochDay: Long,
    val plannedMinutes: Int,
    val actualMinutesCompleted: Int,
    val wasAcknowledgedOnTime: Boolean
)