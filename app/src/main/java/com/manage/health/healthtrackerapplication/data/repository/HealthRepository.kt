package com.manage.health.healthtrackerapplication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.manage.health.healthtrackerapplication.data.dao.HealthDataDao
import com.manage.health.healthtrackerapplication.data.dao.SleepLogDao
import com.manage.health.healthtrackerapplication.data.dao.StepLogDao
import com.manage.health.healthtrackerapplication.data.dao.UserGoalsDao
import com.manage.health.healthtrackerapplication.data.dao.WaterLogDao
import com.manage.health.healthtrackerapplication.data.model.HealthData
import com.manage.health.healthtrackerapplication.data.model.SleepLog
import com.manage.health.healthtrackerapplication.data.model.StepLog
import com.manage.health.healthtrackerapplication.data.model.UserGoals
import com.manage.health.healthtrackerapplication.data.model.WaterLog
import com.manage.health.healthtrackerapplication.data.service.FirebaseDataService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Singleton
class HealthRepository @Inject constructor(
    private val healthDataDao: HealthDataDao,
    private val waterLogDao: WaterLogDao,
    private val stepLogDao: StepLogDao,
    private val sleepLogDao: SleepLogDao,
    private val userGoalsDao: UserGoalsDao,
    private val firebaseDataService: FirebaseDataService,
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun getHealthDataByDate(date: String): HealthData? {
        val userId = getCurrentUserId()
        return if (userId != null) {
            firebaseDataService.getHealthDataByDate(date)
                ?: healthDataDao.getHealthDataByDateAndUser(date, userId)
        } else {
            null
        }
    }

    fun getHealthDateByDataFlow(date: String): Flow<HealthData?> {
        val userId = getCurrentUserId()
        return if (userId != null) {
            healthDataDao.getHealthDataByDateAndUserFlow(date, userId)
        } else {
            flow { emit(null) }
        }
    }

    fun getAllHealthData(): Flow<List<HealthData>> {
        return healthDataDao.getAllHealthData()
    }

    fun getHealthDataBetweenDates(startDate: String, endDate: String): Flow<List<HealthData>> {
        return healthDataDao.getHealthDataBetweenDates(startDate, endDate)
    }

    suspend fun insertOrUpdateHealthData(healthData: HealthData) {
        val userId = getCurrentUserId()
        if (userId != null) {
            val healthDataWithUserId = healthData.copy(userId = userId)
            firebaseDataService.saveHealthData(healthDataWithUserId)
            val existing = healthDataDao.getHealthDataByDateAndUser(healthData.date, userId)
            if (existing != null) {
                healthDataDao.updateHealthData(healthDataWithUserId)
            } else {
                healthDataDao.insertHealthData(healthDataWithUserId)
            }
        }
    }

    fun getWaterLogsByDate(date: String): Flow<List<WaterLog>> {
        return waterLogDao.getWaterLogsByDate(date)
    }

    suspend fun getTotalWaterIntakeForDate(date: String): Int {
        return waterLogDao.getTotalWaterIntakeForDate(date) ?: 0
    }

    suspend fun addWaterIntake(amount: Int, date: String = getCurrentDate()) {
        val userId = getCurrentUserId()
        if (userId != null) {
            val timestamp = getCurrentTimestamp()
            val waterLog = WaterLog(
                userId = userId,
                amount = amount,
                timestamp = timestamp,
                date = date
            )
            firebaseDataService.saveWaterLog(waterLog)
            waterLogDao.insertWaterLog(waterLog)
            updateHealthDataForDate(date)
        }
    }

    fun getStepLogsByDate(date: String): Flow<List<StepLog>> {
        return stepLogDao.getStepLogsByDate(date)
    }

    suspend fun getTotalStepsForDate(date: String): Int {
        return stepLogDao.getTotalStepsForDate(date) ?: 0
    }

    suspend fun addSteps(steps: Int, date: String = getCurrentDate()) {
        val userId = getCurrentUserId()
        if (userId != null) {
            val timestamp = getCurrentTimestamp()
            val stepLog = StepLog(
                userId = userId,
                steps = steps,
                timestamp = timestamp,
                date = date
            )
            firebaseDataService.saveStepLog(stepLog)
            stepLogDao.insertStepLog(stepLog)
            updateHealthDataForDate(date)
        }
    }

    fun getSleepLogsByDate(date: String): Flow<List<SleepLog>> {
        return sleepLogDao.getSleepLogsByDate(date)
    }

    suspend fun getAverageSleepDurationForDate(date: String): Float {
        return sleepLogDao.getAverageSleepDurationForDate(date) ?: 0f
    }

    suspend fun addSleepLog(sleepStart: String, sleepEnd: String, duration: Float, quality: Int = 0, date: String = getCurrentDate()) {
        val userId = getCurrentUserId()
        if (userId != null) {
            val sleepLog = SleepLog(
                userId = userId,
                sleepStart = sleepStart,
                sleepEnd = sleepEnd,
                duration = duration,
                quality = quality,
                date = date
            )
            firebaseDataService.saveSleepLog(sleepLog)
            sleepLogDao.insertSleepLog(sleepLog)
            updateHealthDataForDate(date)
        }
    }

    suspend fun addRunningActivity(steps: Int, distance: Float, duration: Int, calories: Int, date: String = getCurrentDate()) {
        val userId = getCurrentUserId()
        if (userId != null) {
            // Steps update karein pehle
            val timestamp = getCurrentTimestamp()
            val stepLog = StepLog(
                userId = userId,
                steps = steps,
                timestamp = timestamp,
                date = date
            )
            firebaseDataService.saveStepLog(stepLog)
            stepLogDao.insertStepLog(stepLog)


            val totalSteps = getTotalStepsForDate(date)
            val waterIntake = getTotalWaterIntakeForDate(date)
            val sleepHours = getAverageSleepDurationForDate(date)
            val healthScore = calculateHealthScore(totalSteps, waterIntake, sleepHours)

            val existingData = healthDataDao.getHealthDataByDateAndUser(date, userId)
            val updatedData = existingData?.copy(
                steps = totalSteps,
                distance = existingData.distance + distance,
                caloriesBurned = existingData.caloriesBurned + calories,
                healthScore = healthScore,
                updatedAt = getCurrentTimestamp()
            ) ?: HealthData(
                userId = userId,
                date = date,
                steps = totalSteps,
                distance = distance,
                caloriesBurned = calories,
                waterIntake = waterIntake,
                sleepHours = sleepHours,
                healthScore = healthScore,
                createdAt = getCurrentTimestamp(),
                updatedAt = getCurrentTimestamp()
            )
            insertOrUpdateHealthData(updatedData)
        }
    }

    fun getUserGoals(): Flow<UserGoals?> {
        return userGoalsDao.getUserGoals()
    }

    suspend fun updateUserGoals(userGoals: UserGoals) {
        val userId = getCurrentUserId()
        if (userId != null) {
            val userGoalsWithUserId = userGoals.copy(userId = userId)
            firebaseDataService.saveUserGoals(userGoalsWithUserId)
            userGoalsDao.insertUserGoals(userGoalsWithUserId)
        }
    }

    suspend fun getAverageSteps(startDate: String, endDate: String): Float {
        return healthDataDao.getAverageSteps(startDate, endDate) ?: 0f
    }

    suspend fun getAverageWaterIntake(startDate: String, endDate: String): Float {
        return healthDataDao.getAverageWaterIntake(startDate, endDate) ?: 0f
    }

    suspend fun getAverageSleepHours(startDate: String, endDate: String): Float {
        return healthDataDao.getAverageSleepHours(startDate, endDate) ?: 0f
    }

    private suspend fun updateHealthDataForDate(date: String) {
        val userId = getCurrentUserId() ?: return
        val steps = getTotalStepsForDate(date)
        val waterIntake = getTotalWaterIntakeForDate(date)
        val sleepHours = getAverageSleepDurationForDate(date)
        val healthScore = calculateHealthScore(steps, waterIntake, sleepHours)

        val existingData = healthDataDao.getHealthDataByDateAndUser(date, userId)
        val currentTimestamp = getCurrentTimestamp()

        val healthData = existingData?.copy(
            steps = steps,
            waterIntake = waterIntake,
            sleepHours = sleepHours,
            healthScore = healthScore,
            updatedAt = currentTimestamp
        ) ?: HealthData(
            userId = userId,
            date = date,
            steps = steps,
            waterIntake = waterIntake,
            sleepHours = sleepHours,
            healthScore = healthScore,
            createdAt = currentTimestamp,
            updatedAt = currentTimestamp
        )

        insertOrUpdateHealthData(healthData)
    }

    private fun calculateHealthScore(steps: Int, waterIntake: Int, sleepHours: Float): Int {
        val stepsScore = (steps / 10000f * 40).coerceAtMost(40f)
        val waterScore = (waterIntake / 2000f * 30).coerceAtMost(30f)
        val sleepScore = (sleepHours / 8f * 30).coerceAtMost(30f)
        return (stepsScore + waterScore + sleepScore).toInt()
    }

    suspend fun resetAllData() {
        val userId = getCurrentUserId()
        if (userId != null) {
            healthDataDao.deleteAllHealthDataForUser(userId)
            waterLogDao.deleteAllWaterLogsForUser(userId)
            stepLogDao.deleteAllStepLogsForUser(userId)
            sleepLogDao.deleteAllSleepLogsForUser(userId)
            val defaultGoals = UserGoals(userId = userId)
            userGoalsDao.insertUserGoals(defaultGoals)
        }
    }

    suspend fun deleteAllFirebaseData() {
        val userId = getCurrentUserId()
        if (userId != null) {
            firebaseDataService.clearUserData()
        }
    }

    suspend fun syncLocalDataToFirebase() {
        val userId = getCurrentUserId()
        if (userId != null) {
            val healthDataList = healthDataDao.getAllHealthDataForUser(userId)
            val waterLogs = waterLogDao.getAllWaterLogsForUser(userId)
            val stepLogs = stepLogDao.getAllStepsLogsForUser(userId)
            val sleepLogs = sleepLogDao.getAllSleepLogsForUser(userId)
            val userGoals = userGoalsDao.getUserGoalsForUser(userId)


            firebaseDataService.syncAllDataToFirebase(
                healthDataList, waterLogs, stepLogs, sleepLogs, userGoals
            )
        }
    }

    private fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }


    @OptIn(ExperimentalTime::class)
    private fun getCurrentDate(): String {
        val currentMoment = Clock.System.now()
        val localDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
        return localDateTime.date.toString()
    }

    // Fixed: Timestamp ke liye poori date aur time string return karega
    @OptIn(ExperimentalTime::class)
    private fun getCurrentTimestamp(): String {
        val currentMoment = Clock.System.now()
        return currentMoment.toLocalDateTime(TimeZone.currentSystemDefault()).toString()
    }
}