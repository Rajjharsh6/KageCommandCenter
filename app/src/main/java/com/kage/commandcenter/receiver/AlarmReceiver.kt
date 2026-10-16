package com.kage.commandcenter.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kage.commandcenter.data.local.AppDatabase
import com.kage.commandcenter.data.local.EventCategory
import com.kage.commandcenter.data.local.ReminderInstance
import com.kage.commandcenter.data.local.ReminderStatus
import com.kage.commandcenter.engine.KageAlarmScheduler
import com.kage.commandcenter.service.ActiveReminderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra("EXTRA_EVENT_ID", -1L)
        val eventTitle = intent.getStringExtra("EXTRA_EVENT_TITLE") ?: "Task Alert"
        val categoryStr = intent.getStringExtra("EXTRA_CATEGORY") ?: ""
        val scheduledTime = intent.getLongExtra("EXTRA_SCHEDULED_TIME", System.currentTimeMillis())

        if (eventId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context, this)

                val category = try {
                    EventCategory.valueOf(categoryStr)
                } catch (e: Exception) {
                    EventCategory.GENERAL
                }

                val existing = db.reminderDao().getActiveInstanceForEvent(eventId)

                val instanceId: Long = if (existing != null) {
                    existing.instanceId
                } else {
                    val newInstance = ReminderInstance(
                        eventId = eventId,
                        eventTitle = eventTitle,
                        category = category,
                        scheduledEpochMillis = scheduledTime,
                        status = ReminderStatus.ACTIVE_UNACKNOWLEDGED
                    )
                    db.reminderDao().insertInstance(newInstance)
                }

                val serviceIntent = Intent(context, ActiveReminderService::class.java).apply {
                    putExtra("EXTRA_INSTANCE_ID", instanceId)
                    putExtra("EXTRA_EVENT_TITLE", eventTitle)
                    putExtra("EXTRA_CATEGORY", category.name)
                    putExtra("EXTRA_SCHEDULED_TIME", scheduledTime)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }

                val event = db.scheduleDao().getEventById(eventId)
                if (event != null && event.isEnabled) {
                    KageAlarmScheduler.scheduleEvent(context, event)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}