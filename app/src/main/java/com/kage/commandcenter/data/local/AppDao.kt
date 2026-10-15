package com.kage.commandcenter.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM events ORDER BY startHour ASC, startMinute ASC")
    fun getAllEvents(): Flow<List<ScheduleEvent>>

    @Query("SELECT * FROM events WHERE isEnabled = 1")
    suspend fun getActiveEventsSync(): List<ScheduleEvent>

    @Query("SELECT * FROM events WHERE id = :id")
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
    @Query("SELECT * FROM reminder_instances WHERE status = 'ACTIVE_UNACKNOWLEDGED' ORDER BY scheduledEpochMillis DESC")
    fun getActiveReminders(): Flow<List<ReminderInstance>>

    @Query("SELECT * FROM reminder_instances WHERE instanceId = :id")
    suspend fun getInstanceById(id: Long): ReminderInstance?

    @Query("SELECT * FROM reminder_instances WHERE eventId = :eventId AND status = 'ACTIVE_UNACKNOWLEDGED' LIMIT 1")
    suspend fun getActiveInstanceForEvent(eventId: Long): ReminderInstance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstance(instance: ReminderInstance): Long

    @Query("UPDATE reminder_instances SET status = 'ACKNOWLEDGED', acknowledgedAtEpochMillis = :timestamp WHERE instanceId = :instanceId")
    suspend fun acknowledgeInstance(instanceId: Long, timestamp: Long)

    @Query("UPDATE reminder_instances SET status = 'SNOOZED', snoozedUntilEpochMillis = :snoozeUntil WHERE instanceId = :instanceId")
    suspend fun snoozeInstance(instanceId: Long, snoozeUntil: Long)
}

@Dao
interface ProductivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ProductivityLog)
}