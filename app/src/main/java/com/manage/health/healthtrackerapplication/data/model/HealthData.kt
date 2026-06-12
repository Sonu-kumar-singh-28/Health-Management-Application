package com.manage.health.healthtrackerapplication.data.model

import androidx.compose.ui.graphics.FilterQuality
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp


@Entity(tableName = "health_data")
data class HealthData(
    @PrimaryKey
    val date: String,
    val  userId: String = "",
    val  steps: Int = 0,
    val distance: Float = 0f,
    val caloriesBurned: Int =0,
    val waterIntake: Int =0,
    val sleepHours: Float = 0f,
    val heartRate: Int =0,
    val healthScore: Int =0,
    val createdAt: String ="",
    val updatedAt: String = ""
)




@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val  userId: String = "",
    val  amount: Int,
    val timestamp: String,
    val date: String
)


@Entity(tableName = "step_logs")
data class StepLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val  userId: String = "",
    val  steps: Int,
    val timestamp: String,
    val date: String
)


@Entity(tableName = "sleep_logs")
data class SleepLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val  userId: String = "",
    val  sleepStart: String,
    val  sleepEnd: String,
    val duration: Float,
    val quality: Int =0,
    val date: String
)



@Entity(tableName = "user_goals")
data class UserGoals(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 1,
    val  userId: String = "",
    val  dailySteps: Int = 10000,
    val  dailyWater: Int = 200,
    val  dailySleep: Float = 8f,
    val weeklyExercise: Int = 150
)





