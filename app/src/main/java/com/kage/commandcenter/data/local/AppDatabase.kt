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
    version = 3,
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
                        title = "Morning Planning",
                        category = EventCategory.ROUTINE,
                        priority = Priority.HIGH,
                        startHour = 8,
                        startMinute = 30,
                        endHour = 9,
                        endMinute = 0
                    ),
                    ScheduleEvent(
                        title = "Deep Focus Work",
                        category = EventCategory.DEEP_WORK,
                        priority = Priority.CRITICAL,
                        startHour = 9,
                        startMinute = 30,
                        endHour = 12,
                        endMinute = 30
                    ),
                    ScheduleEvent(
                        title = "Evening Routine & Review",
                        category = EventCategory.ROUTINE,
                        priority = Priority.MEDIUM,
                        startHour = 20,
                        startMinute = 0,
                        endHour = 20,
                        endMinute = 30
                    )
                )
                for (event in defaultEvents) {
                    dao.insertEvent(event)
                }
            }
        }
    }
}