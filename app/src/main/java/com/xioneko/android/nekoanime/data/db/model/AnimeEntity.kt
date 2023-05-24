package com.xioneko.android.nekoanime.data.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.xioneko.android.nekoanime.data.db.util.CalendarConverter
import com.xioneko.android.nekoanime.data.db.util.ListConverter
import com.xioneko.android.nekoanime.data.db.util.MapConverter
import com.xioneko.android.nekoanime.data.model.Anime
import java.util.*

@Entity(tableName = "Anime")
@TypeConverters(ListConverter::class, MapConverter::class, CalendarConverter::class)
data class AnimeEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String,
    val latestEpisode: Int,
    val status: String,
    val release: String,
    val genres: List<String>,
    val type: String,
    val year: Int,
    val description: String,
    val videoSource: Map<Int, List<String>>, // episode -> url
    val lastModified: Calendar
)


fun AnimeEntity.asAnime() = Anime(
    id = id,
    name = name,
    imageUrl = imageUrl,
    latestEpisode = latestEpisode,
    status = status,
    release = release,
    genres = genres,
    type = type,
    year = year,
    description = description,
    videoSource = videoSource,
    lastModified = lastModified
)