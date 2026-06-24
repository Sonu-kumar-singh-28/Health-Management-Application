package com.manage.health.healthtrackerapplication.data.model

import com.google.android.gms.maps.model.LatLng

data class WorkOutSession(
    val id: String,
    val startTime: Long,
    val endTime: Long? = null,
    val locations: List<LatLng> = emptyList(),
    val totalDistance: Float = 0f,
    val averagePace: Float = 0f,
    val caloriesBurned: Int = 0,
    val workoutType: WorkoutType = WorkoutType.RUNNING
)

enum class WorkoutType {
    RUNNING,
    WALKING,
    CYCLING,
    HIKING
}