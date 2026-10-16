package com.kage.commandcenter.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromCategory(category: EventCategory?): String = category?.name ?: EventCategory.ROUTINE.name

    @TypeConverter
    fun toCategory(value: String?): EventCategory =
        try {
            EventCategory.valueOf(value ?: EventCategory.ROUTINE.name)
        } catch (e: Exception) {
            EventCategory.ROUTINE
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
    fun fromDaysList(days: List<Int>?): String = days?.joinToString(",") ?: ""

    @TypeConverter
    fun toDaysList(value: String?): List<Int> =
        if (value.isNullOrBlank()) emptyList()
        else value.split(",").mapNotNull { it.trim().toIntOrNull() }
}