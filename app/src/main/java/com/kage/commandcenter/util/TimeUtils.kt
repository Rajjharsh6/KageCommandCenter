package com.kage.commandcenter.util

fun formatTo12Hour(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when (hour) {
        0 -> 12
        in 1..12 -> hour
        else -> hour - 12
    }
    return String.format("%d:%02d %s", displayHour, minute, amPm)
}

fun formatTimeRange(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): String {
    return "${formatTo12Hour(startHour, startMinute)} – ${formatTo12Hour(endHour, endMinute)}"
}