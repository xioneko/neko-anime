package com.xioneko.android.nekoanime.data.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.xioneko.android.nekoanime.data.db.util.CalendarConverter
import com.xioneko.android.nekoanime.data.db.util.ListConverter
import com.xioneko.android.nekoanime.data.db.util.MapConverter
import com.xioneko.android.nekoanime.data.db.util.SetConverter
import com.xioneko.android.nekoanime.data.model.Anime
import java.util.Calendar

@Entity(tableName = "Anime")
@TypeConverters(
    ListConverter::class,
    MapConverter::class,
    SetConverter::class,
    CalendarConverter::class,
)
data class AnimeEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String,
    val status: String,
    val latestEpisode: Int,
    val tags: List<String>,
    val type: String,
    val year: String,
    val description: String,
    val streamIds: Set<Int>,
    val videoSource: Map<Int, String>,
    val lastUpdate: Calendar
)


fun AnimeEntity.asAnime() = Anime(
    id = id,
    name = name,
    imageUrl = imageUrl,
    latestEpisode = latestEpisode,
    status = status,
    type = type,
    tags = tags,
    year = year,
    description = description,
    streamIds = streamIds,
    videoSource = videoSource,
    lastUpdate = lastUpdate
)