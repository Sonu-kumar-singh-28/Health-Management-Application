package com.manage.health.healthtrackerapplication.data.database

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverters
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import java.time.LocalDate

class Converters {

    @TypeConverters
    fun fromLocalData(date: LocalDate?): String?{
        return date?.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverters
    fun toLocalDate(dateString: String?): LocalDate?{
        return dateString?.let {
            LocalDate.parse(it)
        }
    }


    @TypeConverters
    fun fromLocalDateTime(dateTime: LocalTime?): String?{
        return dateTime?.toString()
    }


    @TypeConverters
    fun toLocalDateTime(dateTimeString: LocalTime?): String?{
        return dateTimeString?.let {
            LocalDateTime.parse(it.toString()).toString()
        }
    }


}