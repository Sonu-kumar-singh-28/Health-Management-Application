package com.manage.health.healthtrackerapplication.data.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import com.manage.health.healthtrackerapplication.data.model.HealthData
import com.manage.health.healthtrackerapplication.data.model.SleepLog
import com.manage.health.healthtrackerapplication.data.model.StepLog
import com.manage.health.healthtrackerapplication.data.model.UserGoals
import com.manage.health.healthtrackerapplication.data.model.WaterLog
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDataService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
){
    suspend fun saveHealthData(healthData: HealthData){
        val userId = getCurrentUserId() ?: return
        firestore.collection("users")
            .document(userId)
            .collection("health_data")
            .document(healthData.date)
            .set(healthData, SetOptions.merge())
            .await()
    }

    suspend fun getHealthDataByDate(date: String): HealthData?{
        val userId = getCurrentUserId() ?: return null
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .collection("health_data")
                .document(date)
                .get()
                .await()

            if(document.exists()){
                document.toObject(HealthData::class.java)
            } else {
                null
            }
        } catch (e: Exception){
            null
        }
    }

    suspend fun getAllHealthData(): List<HealthData>{
        val userId = getCurrentUserId() ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("health_data")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject(HealthData::class.java)
            }
        } catch (e: Exception){
            emptyList()
        }
    }

    suspend fun getHealthDataBetweenDates(startDate: String, endDate: String): List<HealthData>{
        val userId = getCurrentUserId() ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("health_data")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull {
                it.toObject(HealthData::class.java)
            }
        } catch (e: Exception){
            emptyList()
        }
    }

    suspend fun saveWaterLog(waterLog: WaterLog){
        val userId = getCurrentUserId() ?: return
        val logId = if(waterLog.id == 0L){
            System.currentTimeMillis().toString()
        } else {
            waterLog.id.toString()
        }

        firestore.collection("users")
            .document(userId)
            .collection("water_logs")
            .document(logId)
            .set(waterLog.copy(id = logId.toLong()))
            .await()
    }

    suspend fun getWaterLogsByDate(date: String): List<WaterLog>{
        val userId = getCurrentUserId() ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("water_logs")
                .whereEqualTo("date", date)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject(WaterLog::class.java)
            }
        } catch (e: Exception){
            emptyList()
        }
    }

    suspend fun saveStepLog(stepLog: StepLog){
        val userId = getCurrentUserId() ?: return
        val logId = if(stepLog.id == 0L){
            System.currentTimeMillis().toString()
        } else {
            stepLog.id.toString()
        }

        firestore.collection("users")
            .document(userId)
            .collection("step_logs")
            .document(logId)
            .set(stepLog.copy(id = logId.toLong()))
            .await()
    }

    suspend fun getStepLogsByDate(date: String): List<StepLog>{
        val userId = getCurrentUserId() ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("step_logs")
                .whereEqualTo("date", date)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject(StepLog::class.java)
            }
        } catch (e: Exception){
            emptyList()
        }
    }

    suspend fun saveSleepLog(sleepLog: SleepLog){
        val userId = getCurrentUserId() ?: return
        val logId = if(sleepLog.id == 0L){
            System.currentTimeMillis().toString()
        } else {
            sleepLog.id.toString()
        }

        firestore.collection("users")
            .document(userId)
            .collection("sleep_logs")
            .document(logId)
            .set(sleepLog.copy(id = logId.toLong()))
            .await()
    }

    suspend fun getSleepLogsByDate(date: String): List<SleepLog>{
        val userId = getCurrentUserId() ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("sleep_logs")
                .whereEqualTo("date", date)
                .orderBy("sleepStart", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject(SleepLog::class.java)
            }
        } catch (e: Exception){
            emptyList()
        }
    }

    suspend fun saveUserGoals(userGoals: UserGoals){
        val userId = getCurrentUserId() ?: return
        firestore.collection("users")
            .document(userId)
            .collection("user_goals")
            .document("goals")
            .set(userGoals)
            .await()
    }

    suspend fun getUserGoals(): UserGoals?{
        val userId = getCurrentUserId() ?: return null
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .collection("user_goals")
                .document("goals")
                .get()
                .await()

            if(document.exists()){
                document.toObject(UserGoals::class.java)
            } else {
                null
            }
        } catch (e: Exception){
            null
        }
    }

    suspend fun syncAllDataToFirebase(
        healthDataList: List<HealthData>,
        waterLogs: List<WaterLog>,
        stepLogs: List<StepLog>,
        sleepLog: List<SleepLog>,
        userGoals: UserGoals?
    ){
        val userId = getCurrentUserId() ?: return

        healthDataList.forEach { healthData ->
            saveHealthData(healthData)
        }

        waterLogs.forEach { waterLog ->
            saveWaterLog(waterLog)
        }

        stepLogs.forEach { stepLog ->
            saveStepLog(stepLog)
        }

        sleepLog.forEach { log ->
            saveSleepLog(log)
        }

        userGoals?.let {
            saveUserGoals(it)
        }
    }

    // Proper suspended function for clearing data
    suspend fun clearUserData(){
        val userId = getCurrentUserId() ?: return

        try {
            val collections = listOf("health_data", "water_logs", "step_logs", "sleep_logs", "user_goals")

            collections.forEach { collectionName ->
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection(collectionName)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val batch = firestore.batch()
                    snapshot.documents.forEach { documentSnapshot ->
                        batch.delete(documentSnapshot.reference)
                    }
                    batch.commit().await() // .await() lagaya taaki operation complete hone ka wait kare
                }
            }
        } catch (e: Exception){
            // Handle Error or log it
            e.printStackTrace()
        }
    }

    private fun getCurrentUserId(): String?{
        return firebaseAuth.currentUser?.uid
    }

    fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }
}