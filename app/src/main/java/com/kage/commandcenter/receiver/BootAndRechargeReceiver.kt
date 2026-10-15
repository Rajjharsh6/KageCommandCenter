package com.kage.commandcenter.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kage.commandcenter.data.local.AppDatabase
import com.kage.commandcenter.engine.KageAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootAndRechargeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val actions = listOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED
        )

        if (intent.action in actions) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context, this)
                    val activeEvents = db.scheduleDao().getActiveEventsSync()
                    for (event in activeEvents) {
                        KageAlarmScheduler.scheduleEvent(context, event)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}