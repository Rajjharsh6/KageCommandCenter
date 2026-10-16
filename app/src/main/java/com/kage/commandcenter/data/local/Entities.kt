package com.kage.commandcenter.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EventCategory {
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

@Entity(tableName = "schedule_events")
data class ScheduleEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "category")
    val category: EventCategory,
    @ColumnInfo(name = "priority")
    val priority: Priority,
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
    val id: Long = 0L,
    @ColumnInfo(name = "eventId")
    val eventId: Long,
    @ColumnInfo(name = "eventTitle")
    val eventTitle: String,
    @ColumnInfo(name = "category")
    val category: EventCategory,
    @ColumnInfo(name = "triggerEpochMillis")
    val triggerEpochMillis: Long,
    @ColumnInfo(name = "isAcknowledged")
    val isAcknowledged: Boolean = false,
    @ColumnInfo(name = "acknowledgedEpochMillis")
    val acknowledgedEpochMillis: Long? = null
)

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