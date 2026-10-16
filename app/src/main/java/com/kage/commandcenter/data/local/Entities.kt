package com.kage.commandcenter.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EventCategory {
    GENERAL,
    JEE_STUDY,
    TRADING,
    GAMING,
    FITNESS,
    DEEP_WORK,
    CHORES,
    ROUTINE
}

enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class ReminderStatus {
    ACTIVE_UNACKNOWLEDGED,
    ACTIVE_ACKNOWLEDGED,
    SNOOZED,
    CANCELLED
}

@Entity(tableName = "schedule_events")
data class ScheduleEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "category")
    val category: EventCategory = EventCategory.ROUTINE,
    @ColumnInfo(name = "priority")
    val priority: Priority = Priority.MEDIUM,
    @ColumnInfo(name = "startHour")
    val startHour: Int,
    @ColumnInfo(name = "startMinute")
    val startMinute: Int,
    @ColumnInfo(name = "endHour")
    val endHour: Int,
    @ColumnInfo(name = "endMinute")
    val endMinute: Int,
    @ColumnInfo(name = "daysOfWeek")
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),
    @ColumnInfo(name = "isEnabled")
    val isEnabled: Boolean = true
)

@Entity(tableName = "reminder_instances")
data class ReminderInstance(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "instanceId")
    val instanceId: Long = 0L,
    @ColumnInfo(name = "eventId")
    val eventId: Long,
    @ColumnInfo(name = "eventTitle")
    val eventTitle: String,
    @ColumnInfo(name = "category")
    val category: EventCategory = EventCategory.ROUTINE,
    @ColumnInfo(name = "scheduledEpochMillis")
    val scheduledEpochMillis: Long,
    @ColumnInfo(name = "status")
    val status: ReminderStatus = ReminderStatus.ACTIVE_UNACKNOWLEDGED,
    @ColumnInfo(name = "acknowledgedEpochMillis")
    val acknowledgedEpochMillis: Long? = null
) {
    // Aliases to ensure backward compatibility with all screens
    val id: Long get() = instanceId
    val triggerEpochMillis: Long get() = scheduledEpochMillis
    val isAcknowledged: Boolean get() = status == ReminderStatus.ACTIVE_ACKNOWLEDGED
}

@Entity(tableName = "productivity_logs")
data class ProductivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "eventId")
    val eventId: Long,
    @ColumnInfo(name = "category")
    val category: EventCategory,
    @ColumnInfo(name = "dateEpochDay")
    val dateEpochDay: Long,
    @ColumnInfo(name = "plannedMinutes")
    val plannedMinutes: Int,
    @ColumnInfo(name = "actualMinutesCompleted")
    val actualMinutesCompleted: Int,
    @ColumnInfo(name = "wasAcknowledgedOnTime")
    val wasAcknowledgedOnTime: Boolean
)