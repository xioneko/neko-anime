package com.xioneko.android.nekoanime.data.db.util

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar

class ListConverter {
    @TypeConverter
    fun ListToJson(value: List<String>) = Json.encodeToString(value)

    @TypeConverter
    fun JsonToList(value: String): List<String> = Json.decodeFromString(value)
}

class MapConverter {
    @TypeConverter
    fun MapToJson(value: Map<Int, List<String>>) = Json.encodeToString(value)

    @TypeConverter
    fun JsonToMap(value: String): Map<Int, List<String>> = Json.decodeFromString(value)
}

class CalendarConverter {
    @TypeConverter
    fun LongToCalendar(value: Long): Calendar {
        return Calendar.Builder().setInstant(value).build()
    }

    @TypeConverter
    fun CalendarToLong(date: Calendar): Long {
        return date.timeInMillis
    }
}