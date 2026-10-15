package com.kage.commandcenter.engine

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kage.commandcenter.data.local.ScheduleEvent
import com.kage.commandcenter.receiver.AlarmReceiver
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object KageAlarmScheduler {
    private val KOLKATA_ZONE: ZoneId = ZoneId.of("Asia/Kolkata")

    fun scheduleEvent(context: Context, event: ScheduleEvent) {
        if (!event.isEnabled) {
            cancelEvent(context, event.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val nextTriggerEpoch = calculateNextOccurrence(event)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.kage.ACTION_TRIGGER_EVENT"
            putExtra("EXTRA_EVENT_ID", event.id)
            putExtra("EXTRA_EVENT_TITLE", event.title)
            putExtra("EXTRA_CATEGORY", event.category.name)
            putExtra("EXTRA_SCHEDULED_TIME", nextTriggerEpoch)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerEpoch, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerEpoch, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerEpoch, pendingIntent)
            }
        } catch (_: SecurityException) { }
    }

    fun cancelEvent(context: Context, eventId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.kage.ACTION_TRIGGER_EVENT"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun calculateNextOccurrence(event: ScheduleEvent, nowZoned: ZonedDateTime = ZonedDateTime.now(KOLKATA_ZONE)): Long {
        val today = nowZoned.toLocalDate()
        val targetTime = LocalTime.of(event.startHour, event.startMinute)

        if (event.daysOfWeek.isEmpty()) {
            var candidate = LocalDateTime.of(today, targetTime).atZone(KOLKATA_ZONE)
            if (candidate.isBefore(nowZoned) || candidate.isEqual(nowZoned)) {
                candidate = candidate.plusDays(1)
            }
            return candidate.toInstant().toEpochMilli()
        }

        for (i in 0..7) {
            val candidateDate = today.plusDays(i.toLong())
            val dayVal = candidateDate.dayOfWeek.value
            if (event.daysOfWeek.contains(dayVal)) {
                val candidateDateTime = LocalDateTime.of(candidateDate, targetTime).atZone(KOLKATA_ZONE)
                if (candidateDateTime.isAfter(nowZoned)) {
                    return candidateDateTime.toInstant().toEpochMilli()
                }
            }
        }

        return nowZoned.plusDays(7).toInstant().toEpochMilli()
    }
}