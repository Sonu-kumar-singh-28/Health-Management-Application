package com.manage.health.healthtrackerapplication.data.service

import com.manage.health.healthtrackerapplication.data.model.HealthTips
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class HealthTipsServices {

    suspend fun getHealthTips(category: String = "general"): List<HealthTips> = withContext(Dispatchers.IO) {
        try {
            getStaticHealthTips(category)
        } catch (e: Exception) {
            getFallBackTips()
        }
    }

    private fun getStaticHealthTips(category: String): List<HealthTips> {
        return when (category.lowercase().trim()) {
            "hydration" -> listOf(
                HealthTips(
                    id = "1",
                    title = "Stay Hydrated",
                    description = "Drink at least 8 glasses of water daily. Start your morning with a glass of water to kickstart your metabolism.",
                    category = "hydration"
                ),
                HealthTips(
                    id = UUID.randomUUID().toString(),
                    title = "Pre-Workout Fluids",
                    description = "Drink 500ml of water 2 hours before exercising to maintain peak physical performance.",
                    category = "hydration"
                )
            )

            "nutrition", "nutration" -> listOf(
                HealthTips(
                    id = UUID.randomUUID().toString(),
                    title = "Embrace Whole Foods",
                    description = "Focus on nutrient-dense dietary sources. Prioritize complex carbs, colorful leafy greens, and lean proteins.",
                    category = "nutrition"
                ),
                HealthTips(
                    id = UUID.randomUUID().toString(),
                    title = "Reduce Refined Sugars",
                    description = "Cut down on carbonated soft drinks and packed snacks to protect your continuous glycemic balance.",
                    category = "nutrition"
                )
            )

            "exercise" -> listOf(
                HealthTips(
                    id = UUID.randomUUID().toString(),
                    title = "Consistent Cardio Integration",
                    description = "Aim for at least 150 minutes of moderate-intensity aerobic exercise, such as brisk walking or cycling, weekly.",
                    category = "exercise"
                ),
                HealthTips(
                    id = UUID.randomUUID().toString(),
                    title = "Active Desk Stretching",
                    description = "Stand up and stretch for 5 minutes after every hour of continuous sitting to relieve spinal tension.",
                    category = "exercise"
                )
            )

            "sleep" -> listOf(
                HealthTips(
                    id = UUID.randomUUID().toString(),
                    title = "Establish Fixed Sleep Schedules",
                    description = "Maintain a regular sleep-wake schedule by going to bed and waking up at the same time every single day.",
                    category = "sleep"
                ),
                HealthTips(
                    id = UUID.randomUUID().toString(),
                    title = "Digital Screen Detox Routine",
                    description = "Turn off phones and monitors at least 45-60 minutes before hitting the bed to support optimal sleep.",
                    category = "sleep"
                )
            )

            // General category fallback
            else -> getFallBackTips()
        }
    }

    private fun getFallBackTips(): List<HealthTips> {
        return listOf(
            HealthTips(
                id = "fallback1",
                title = "Avoid Sitting Too Long",
                description = "Prolonged sitting can have negative health effects. Try to stand up, stretch, or take a short walk every hour.",
                category = "general",
                source = "Health Api"
            ),
            HealthTips(
                id = UUID.randomUUID().toString(),
                title = "Hydrate Regularly",
                description = "Staying hydrated is essential for overall health. Aim to drink water consistently throughout the day.",
                category = "hydration",
                source = "Health Api"
            ),
            HealthTips(
                id = UUID.randomUUID().toString(),
                title = "Prioritize Sleep",
                description = "Quality sleep is vital for mental and physical well-being. Aim for 7-9 hours of restful sleep each night.",
                category = "sleep",
                source = "Health Api"
            ),
            HealthTips(
                id = UUID.randomUUID().toString(),
                title = "Eat a Balanced Diet",
                description = "Fuel your body with a variety of nutrient-rich foods, including fruits, vegetables, lean proteins, and whole grains.",
                category = "nutrition",
                source = "Health Api"
            )
        )
    }



    suspend fun getRandomTip(): HealthTips = withContext(context = Dispatchers.IO) {
        val allTips = getStaticHealthTips(category = "general") +
                getStaticHealthTips(category = "hydration") +
                getStaticHealthTips(category = "exercise") +
                getStaticHealthTips(category = "sleep") +
                getStaticHealthTips(category = "nutrition")

        allTips.random()
    }
}