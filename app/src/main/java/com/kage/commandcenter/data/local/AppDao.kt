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
    @Query("SELECT * FROM reminder_instances WHERE status = 'ACTIVE_UNACKNOWLEDGED' ORDER BY scheduledEpochMillis ASC")
    fun getActiveReminders(): Flow<List<ReminderInstance>>

    @Query("SELECT * FROM reminder_instances WHERE instanceId = :id")
    suspend fun getInstanceById(id: Long): ReminderInstance?

    @Query("SELECT * FROM reminder_instances WHERE eventId = :eventId AND status = 'ACTIVE_UNACKNOWLEDGED' LIMIT 1")
    suspend fun getActiveInstanceForEvent(eventId: Long): ReminderInstance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstance(instance: ReminderInstance): Long

    @Query("UPDATE reminder_instances SET status = 'ACTIVE_ACKNOWLEDGED', acknowledgedEpochMillis = :acknowledgedAt WHERE instanceId = :id")
    suspend fun acknowledgeInstance(id: Long, acknowledgedAt: Long)

    @Query("UPDATE reminder_instances SET status = 'SNOOZED', scheduledEpochMillis = :snoozeUntil WHERE instanceId = :id")
    suspend fun snoozeInstance(id: Long, snoozeUntil: Long)
}

@Dao
interface ProductivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ProductivityLog)

    @Query("SELECT * FROM productivity_logs ORDER BY dateEpochDay DESC")
    fun getAllLogs(): Flow<List<ProductivityLog>>
}