package com.manage.health.healthtrackerapplication.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.manage.health.healthtrackerapplication.data.dao.*
import com.manage.health.healthtrackerapplication.data.model.*

@Database(
    entities = [
        HealthData::class,
        WaterLog::class,
        StepLog::class,
        SleepLog::class,
        UserGoals::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HealthDataBase: RoomDatabase(){

    abstract fun healthDataDao(): HealthDataDao
    abstract fun waterLogDao(): WaterLogDao
    abstract fun stepLogDao(): StepLogDao
    abstract fun sleepLogDao(): SleepLogDao
    abstract fun userGoalsDao(): UserGoalsDao

    companion object {

        @Volatile
        private var INSTANCE: HealthDataBase? = null

        fun getDataBase(context: Context, userId: String? = null): HealthDataBase {
            val dataBaseName = if (userId != null) "health_database_$userId" else "health_database"

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HealthDataBase::class.java,
                    dataBaseName
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }

    fun clearInstance(){
        synchronized(this){
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}