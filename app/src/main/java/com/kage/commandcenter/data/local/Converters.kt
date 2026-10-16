package com.kage.commandcenter.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromCategory(category: EventCategory?): String = category?.name ?: EventCategory.GENERAL.name

    @TypeConverter
    fun toCategory(value: String?): EventCategory =
        try {
            EventCategory.valueOf(value ?: EventCategory.GENERAL.name)
        } catch (e: Exception) {
            EventCategory.GENERAL
        }

    @TypeConverter
    fun fromPriority(priority: Priority?): String = priority?.name ?: Priority.MEDIUM.name

    @TypeConverter
    fun toPriority(value: String?): Priority =
        try {
            Priority.valueOf(value ?: Priority.MEDIUM.name)
        } catch (e: Exception) {
            Priority.MEDIUM
        }

    @TypeConverter
    fun fromReminderStatus(status: ReminderStatus?): String =
        status?.name ?: ReminderStatus.ACTIVE_UNACKNOWLEDGED.name

    @TypeConverter
    fun toReminderStatus(value: String?): ReminderStatus =
        try {
            ReminderStatus.valueOf(value ?: ReminderStatus.ACTIVE_UNACKNOWLEDGED.name)
        } catch (e: Exception) {
            ReminderStatus.ACTIVE_UNACKNOWLEDGED
        }

    @TypeConverter
    fun fromDaysList(days: List<Int>?): String = days?.joinToString(",") ?: ""

    @TypeConverter
    fun toDaysList(value: String?): List<Int> =
        if (value.isNullOrBlank()) emptyList()
        else value.split(",").mapNotNull { it.trim().toIntOrNull() }
}