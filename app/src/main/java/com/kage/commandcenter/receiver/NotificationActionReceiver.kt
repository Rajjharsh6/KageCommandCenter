package com.kage.commandcenter.receiver

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kage.commandcenter.data.local.AppDatabase
import com.kage.commandcenter.data.local.ProductivityLog
import com.kage.commandcenter.service.ActiveReminderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val instanceId = intent.getLongExtra("EXTRA_INSTANCE_ID", -1L)
        if (instanceId == -1L) return

        val intentAction = intent.action
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context, this)
                val instance = db.reminderDao().getInstanceById(instanceId)

                if (intentAction == "com.kage.ACTION_ACKNOWLEDGE") {
                    db.reminderDao().acknowledgeInstance(instanceId, System.currentTimeMillis())

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(instanceId.toInt())

                    val stopServiceIntent = Intent(context, ActiveReminderService::class.java)
                    context.stopService(stopServiceIntent)

                    if (instance != null) {
                        val event = db.scheduleDao().getEventById(instance.eventId)
                        val plannedMin = if (event != null) {
                            var duration = (event.endHour * 60 + event.endMinute) - (event.startHour * 60 + event.startMinute)
                            if (duration < 0) duration += 24 * 60
                            duration
                        } else 60

                        db.productivityDao().insertLog(
                            ProductivityLog(
                                eventId = instance.eventId,
                                category = instance.category,
                                dateEpochDay = LocalDate.now().toEpochDay(),
                                plannedMinutes = plannedMin,
                                actualMinutesCompleted = plannedMin,
                                wasAcknowledgedOnTime = true
                            )
                        )
                    }
                } else if (intentAction == "com.kage.ACTION_SNOOZE") {
                    val snoozeMinutes = intent.getIntExtra("EXTRA_SNOOZE_MINUTES", 10)
                    val snoozeUntil = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

                    db.reminderDao().snoozeInstance(instanceId, snoozeUntil)

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(instanceId.toInt())

                    val stopServiceIntent = Intent(context, ActiveReminderService::class.java)
                    context.stopService(stopServiceIntent)

                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                        this.action = "com.kage.ACTION_TRIGGER_EVENT"
                        putExtra("EXTRA_EVENT_ID", instance?.eventId ?: -1L)
                        putExtra("EXTRA_EVENT_TITLE", instance?.eventTitle ?: "Snoozed Task")
                        putExtra("EXTRA_CATEGORY", instance?.category?.name)
                        putExtra("EXTRA_SCHEDULED_TIME", snoozeUntil)
                    }
                    val pi = PendingIntent.getBroadcast(
                        context,
                        (instanceId + 50000).toInt(),
                        alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeUntil, pi)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}