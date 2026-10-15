package com.kage.commandcenter.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.kage.commandcenter.MainActivity
import com.kage.commandcenter.data.local.AppDatabase
import com.kage.commandcenter.receiver.NotificationActionReceiver
import kotlinx.coroutines.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ActiveReminderService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var wakeLock: PowerManager.WakeLock? = null
    private val channelId = "kage_urgent_reminders"
    private val nagLoopDurationMs = 30_000L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val instanceId = intent?.getLongExtra("EXTRA_INSTANCE_ID", -1L) ?: -1L
        val eventTitle = intent?.getStringExtra("EXTRA_EVENT_TITLE") ?: "Scheduled Task"
        val scheduledTime = intent?.getLongExtra("EXTRA_SCHEDULED_TIME", System.currentTimeMillis()) ?: System.currentTimeMillis()

        if (instanceId == -1L) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(instanceId.toInt(), buildNotification(instanceId, eventTitle, scheduledTime, 1))

        serviceScope.launch {
            val database = AppDatabase.getDatabase(applicationContext, this)
            var currentIteration = 1

            while (isActive) {
                val instance = database.reminderDao().getInstanceById(instanceId)
                if (instance == null || instance.status.name != "ACTIVE_UNACKNOWLEDGED") {
                    break
                }

                triggerSensoryAlert()

                val notification = buildNotification(instanceId, eventTitle, scheduledTime, currentIteration)
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(instanceId.toInt(), notification)

                currentIteration++
                delay(nagLoopDurationMs)
            }

            stopSelf()
        }

        return START_REDELIVER_INTENT
    }

    private fun buildNotification(
        instanceId: Long,
        title: String,
        scheduledEpoch: Long,
        nagCount: Int
    ): Notification {
        val formattedTime = Instant.ofEpochMilli(scheduledEpoch)
            .atZone(ZoneId.of("Asia/Kolkata"))
            .format(DateTimeFormatter.ofPattern("hh:mm a"))

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            instanceId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val ackIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "com.kage.ACTION_ACKNOWLEDGE"
            putExtra("EXTRA_INSTANCE_ID", instanceId)
        }
        val ackPendingIntent = PendingIntent.getBroadcast(
            this,
            (instanceId * 10 + 1).toInt(),
            ackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "com.kage.ACTION_SNOOZE"
            putExtra("EXTRA_INSTANCE_ID", instanceId)
            putExtra("EXTRA_SNOOZE_MINUTES", 10)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            (instanceId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("⚠️ AWAITING ACKNOWLEDGMENT: $title")
            .setContentText("Scheduled at $formattedTime • Repeated $nagCount time(s)")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .addAction(android.R.drawable.checkbox_on_background, "✅ OK, DONE", ackPendingIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "⏳ Snooze 10m", snoozePendingIntent)
            .build()
    }

    private fun triggerSensoryAlert() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 400, 200, 400), -1)
            }

            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
            ringtone?.play()
        } catch (_: Exception) { }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "KAGE::ActiveReminderWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "KAGE Command Center Nag Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Continuous repeats until unacknowledged reminders are closed"
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        serviceJob.cancel()
        try {
            if (wakeLock?.isHeld == true) wakeLock?.release()
        } catch (_: Exception) { }
        super.onDestroy()
    }
}