package com.xioneko.android.nekoanime.data.db.util

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar

class ListConverter {
    @TypeConverter
    fun listToJson(value: List<String>) = Json.encodeToString(value)

    @TypeConverter
    fun jsonToList(value: String): List<String> = Json.decodeFromString(value)
}

class MapConverter {
    @TypeConverter
    fun mapToJson(value: Map<Int, String>) = Json.encodeToString(value)

    @TypeConverter
    fun jsonToMap(value: String): Map<Int, String> = Json.decodeFromString(value)
}

class SetConverter {
    @TypeConverter
    fun setToJson(value: Set<Int>) = Json.encodeToString(value)

    @TypeConverter
    fun jsonToSet(value: String): Set<Int> = Json.decodeFromString(value)
}

class CalendarConverter {
    @TypeConverter
    fun longToCalendar(value: Long): Calendar {
        return Calendar.Builder().setInstant(value).build()
    }

    @TypeConverter
    fun calendarToLong(date: Calendar): Long {
        return date.timeInMillis
    }
}