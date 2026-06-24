package com.manage.health.healthtrackerapplication.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.manage.health.healthtrackerapplication.data.model.HealthData
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDataDao {

    @Query("SELECT * FROM health_data WHERE date = :date")
    suspend fun getHealthDataByDate(date: String): HealthData?

    @Query("SELECT * FROM health_data WHERE date = :date AND userId = :userId")
    suspend fun getHealthDataByDateAndUser(date: String, userId: String): HealthData?

    @Query("SELECT * FROM health_data WHERE date = :date")
    fun getHealthDataByDateFlow(date: String): Flow<HealthData?>

    @Query("SELECT * FROM health_data WHERE date = :date AND userId = :userId")
    fun getHealthDataByDateAndUserFlow(date: String, userId: String): Flow<HealthData?>

    @Query("SELECT * FROM health_data ORDER BY date DESC")
    fun getAllHealthData(): Flow<List<HealthData>> // Fix: Removed unused date parameter

    @Query("SELECT * FROM health_data WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllHealthDataForUser(userId: String): List<HealthData> // Fix: Changed date to userId

    @Query("SELECT * FROM health_data WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getHealthDataBetweenDates(startDate: String, endDate: String): Flow<List<HealthData>> // Fix: Renamed and removed suspend

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthData(healthData: HealthData)

    @Update
    suspend fun updateHealthData(healthData: HealthData)

    @Delete
    suspend fun deleteHealthData(healthData: HealthData)

    @Query("SELECT AVG(steps) FROM health_data WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageSteps(startDate: String, endDate: String): Float?

    @Query("SELECT AVG(waterIntake) FROM health_data WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageWaterIntake(startDate: String, endDate: String): Float?

    @Query("SELECT AVG(sleepHours) FROM health_data WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageSleepHours(startDate: String, endDate: String): Float?

    @Query("DELETE FROM health_data")
    suspend fun deleteAllHealthData()

    @Query("DELETE FROM health_data WHERE userId = :userId")
    suspend fun deleteAllHealthDataForUser(userId: String) // Fix: Added for repository support
}