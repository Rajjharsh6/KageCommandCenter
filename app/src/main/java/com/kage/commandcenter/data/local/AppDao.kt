package com.kage.commandcenter.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_events ORDER BY startHour ASC, startMinute ASC")
    fun getAllEvents(): Flow<List<ScheduleEvent>>

    @Query("SELECT * FROM schedule_events WHERE isEnabled = 1")
    suspend fun getActiveEventsSync(): List<ScheduleEvent>

    @Query("SELECT * FROM schedule_events WHERE id = :id")
    suspend fun getEventById(id: Long): ScheduleEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: ScheduleEvent): Long

    @Update
    suspend fun updateEvent(event: ScheduleEvent)

    @Delete
    suspend fun deleteEvent(event: ScheduleEvent)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminder_instances WHERE isAcknowledged = 0 ORDER BY triggerEpochMillis ASC")
    fun getActiveReminders(): Flow<List<ReminderInstance>>

    @Query("SELECT * FROM reminder_instances WHERE id = :id")
    suspend fun getInstanceById(id: Long): ReminderInstance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstance(instance: ReminderInstance): Long

    @Query("UPDATE reminder_instances SET isAcknowledged = 1, acknowledgedEpochMillis = :acknowledgedAt WHERE id = :id")
    suspend fun acknowledgeInstance(id: Long, acknowledgedAt: Long)

    @Query("UPDATE reminder_instances SET triggerEpochMillis = :snoozeUntil WHERE id = :id")
    suspend fun snoozeInstance(id: Long, snoozeUntil: Long)
}

@Dao
interface ProductivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ProductivityLog)

    @Query("SELECT * FROM productivity_logs ORDER BY dateEpochDay DESC")
    fun getAllLogs(): Flow<List<ProductivityLog>>
}