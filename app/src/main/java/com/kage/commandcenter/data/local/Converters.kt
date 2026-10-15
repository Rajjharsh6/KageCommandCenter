package com.kage.commandcenter.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromIntList(list: List<Int>?): String = list?.joinToString(",") ?: ""

    @TypeConverter
    fun toIntList(data: String): List<Int> {
        if (data.isBlank()) return emptyList()
        return data.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    @TypeConverter
    fun fromCategory(category: EventCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): EventCategory = runCatching {
        EventCategory.valueOf(value)
    }.getOrDefault(EventCategory.GENERAL)

    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority = runCatching {
        Priority.valueOf(value)
    }.getOrDefault(Priority.MEDIUM)

    @TypeConverter
    fun fromStatus(status: ReminderStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): ReminderStatus = runCatching {
        ReminderStatus.valueOf(value)
    }.getOrDefault(ReminderStatus.SCHEDULED)
}