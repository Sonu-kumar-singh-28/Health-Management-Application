package com.manage.health.healthtrackerapplication.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.manage.health.healthtrackerapplication.data.model.SleepLog
import kotlinx.coroutines.flow.Flow

@Dao // Fix: Added Missing Annotation
interface SleepLogDao {
    @Query("SELECT * FROM sleep_logs WHERE date = :date ORDER BY sleepStart DESC")
    fun getSleepLogsByDate(date: String): Flow<List<SleepLog>>

    @Query("SELECT AVG(duration) FROM sleep_logs WHERE date = :date")
    suspend fun getAverageSleepDurationForDate(date: String): Float? // Fix: Added suspend since it's not a Flow

    @Query("SELECT * FROM sleep_logs ORDER BY sleepStart DESC LIMIT :limit")
    fun getRecentSleepLogs(limit: Int = 30): Flow<List<SleepLog>>

    @Insert
    suspend fun insertSleepLog(sleepLog: SleepLog)

    @Update
    suspend fun updateSleepLog(sleepLog: SleepLog)

    @Delete
    suspend fun deleteSleepLog(sleepLog: SleepLog)

    @Query("DELETE FROM sleep_logs WHERE id = :id")
    suspend fun deleteSleepLogById(id: Long)

    @Query("SELECT * FROM sleep_logs WHERE userId = :userId ORDER BY sleepStart DESC ")
    suspend fun getAllSleepLogsForUser(userId: String): List<SleepLog>

    @Query("DELETE FROM sleep_logs WHERE userId = :userId")
    suspend fun deleteAllSleepLogsForUser(userId: String)
}