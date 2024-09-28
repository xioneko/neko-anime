package com.xioneko.android.nekoanime.data.model

import com.xioneko.android.nekoanime.data.model.util.CalendarAsLongSerializer
import kotlinx.serialization.Serializable
import java.util.Calendar

@Serializable
data class SearchRecord(
    val value: String
)

interface DatedRecord {
    val date: Calendar
}

/**
 * 按日期从新到旧排序，得到一个 records list
 */
fun <T : DatedRecord> Map<Int, T>.sortedByDate() =
    entries.sortedByDescending { it.value.date }

@Serializable
data class WatchRecord(
    @Serializable(with = CalendarAsLongSerializer::class)
    override val date: Calendar,
    val recentEpisode: Int,
    val positions: Map<Int, Long>,
    val progress: Map<Int, Int>, // episode -> percentage
) : DatedRecord
