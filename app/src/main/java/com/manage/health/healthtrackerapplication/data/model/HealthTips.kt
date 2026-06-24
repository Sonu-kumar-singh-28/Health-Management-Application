package com.manage.health.healthtrackerapplication.data.model

data class HealthTips(
    val id: String,
    val  title: String,
    val description: String,
    val category: String,
    val source: String = "Health Api"
)
