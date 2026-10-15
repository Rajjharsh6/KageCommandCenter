package com.kage.commandcenter.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ScheduleEvent::class, ReminderInstance::class, ProductivityLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun reminderDao(): ReminderDao
    abstract fun productivityDao(): ProductivityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kage_command_center.db"
                ).addCallback(DatabaseCallback(scope))
                 .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialRoutine(database.scheduleDao())
                    }
                }
            }

            private suspend fun populateInitialRoutine(dao: ScheduleDao) {
                val allDays = listOf(1, 2, 3, 4, 5, 6, 7)
                val sampleRoutine = listOf(
                    ScheduleEvent(title = "Morning Wake Up & Hydration", category = EventCategory.GENERAL, priority = Priority.HIGH, startHour = 5, startMinute = 0, endHour = 5, endMinute = 30, daysOfWeek = allDays),
                    ScheduleEvent(title = "JEE Physics: Mechanics & Formulas Revision", category = EventCategory.JEE_STUDY, priority = Priority.CRITICAL, startHour = 5, startMinute = 30, endHour = 8, endMinute = 0, daysOfWeek = allDays),
                    ScheduleEvent(title = "Healthy Breakfast & Fresh Air", category = EventCategory.BREAK, priority = Priority.MEDIUM, startHour = 8, startMinute = 0, endHour = 9, endMinute = 0, daysOfWeek = allDays),
                    ScheduleEvent(title = "JEE Mathematics: Problem Solving Drill", category = EventCategory.JEE_STUDY, priority = Priority.CRITICAL, startHour = 9, startMinute = 0, endHour = 10, endMinute = 30, daysOfWeek = allDays),
                    ScheduleEvent(title = "Live Trading Class & Market Analysis", category = EventCategory.TRADING, priority = Priority.HIGH, startHour = 10, startMinute = 30, endHour = 13, endMinute = 0, daysOfWeek = listOf(1, 2, 3, 4, 5)),
                    ScheduleEvent(title = "Lunch & Post-Trade Journaling", category = EventCategory.BREAK, priority = Priority.MEDIUM, startHour = 13, startMinute = 0, endHour = 14, endMinute = 0, daysOfWeek = allDays),
                    ScheduleEvent(title = "JEE Chemistry & Backlog Resolution", category = EventCategory.JEE_STUDY, priority = Priority.CRITICAL, startHour = 14, startMinute = 0, endHour = 16, endMinute = 0, daysOfWeek = allDays),
                    ScheduleEvent(title = "Guilt-Free Gaming & Relaxation", category = EventCategory.GAMING, priority = Priority.LOW, startHour = 16, startMinute = 0, endHour = 18, endMinute = 0, daysOfWeek = allDays),
                    ScheduleEvent(title = "JEE Daily Practice Problems (DPP)", category = EventCategory.JEE_STUDY, priority = Priority.CRITICAL, startHour = 18, startMinute = 0, endHour = 20, endMinute = 30, daysOfWeek = allDays),
                    ScheduleEvent(title = "Nightly Review & Sleep Prep", category = EventCategory.REVIEW, priority = Priority.HIGH, startHour = 21, startMinute = 30, endHour = 22, endMinute = 0, daysOfWeek = allDays),
                    ScheduleEvent(title = "Sleep & Deep Recovery", category = EventCategory.SLEEP, priority = Priority.HIGH, startHour = 22, startMinute = 0, endHour = 5, endMinute = 0, daysOfWeek = allDays)
                )
                sampleRoutine.forEach { dao.insertEvent(it) }
            }
        }
    }
}