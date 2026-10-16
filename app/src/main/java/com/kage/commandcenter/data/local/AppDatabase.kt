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
    version = 2,
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
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialRoutine(database.scheduleDao())
                    }
                }
            }

            suspend fun populateInitialRoutine(dao: ScheduleDao) {
                val defaultEvents = listOf(
                    ScheduleEvent(
                        title = "Trading Analysis & Prep",
                        category = EventCategory.TRADING,
                        priority = Priority.HIGH,
                        startHour = 9,
                        startMinute = 15,
                        endHour = 10,
                        endMinute = 30
                    ),
                    ScheduleEvent(
                        title = "JEE Physics / Math Deep Work",
                        category = EventCategory.JEE_STUDY,
                        priority = Priority.CRITICAL,
                        startHour = 11,
                        startMinute = 0,
                        endHour = 13,
                        endMinute = 30
                    ),
                    ScheduleEvent(
                        title = "JEE Chemistry & Problem Revision",
                        category = EventCategory.JEE_STUDY,
                        priority = Priority.CRITICAL,
                        startHour = 15,
                        startMinute = 0,
                        endHour = 17,
                        endMinute = 30
                    ),
                    ScheduleEvent(
                        title = "Workout & Reset",
                        category = EventCategory.FITNESS,
                        priority = Priority.MEDIUM,
                        startHour = 18,
                        startMinute = 0,
                        endHour = 19,
                        endMinute = 0
                    )
                )
                for (event in defaultEvents) {
                    dao.insertEvent(event)
                }
            }
        }
    }
}