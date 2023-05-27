package com.xioneko.android.nekoanime.data.model

import com.xioneko.android.nekoanime.data.db.model.AnimeEntity
import java.util.Calendar

data class Anime(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val latestEpisode: Int,
    val status: String,
    val release: String,
    val genres: List<String> = emptyList(),
    val type: String,
    val year: Int,
    val description: String,
    val videoSource: Map<Int, List<String>> = emptyMap(), // episode -> url
    val lastModified: Calendar
)

fun Anime.asAnimeShell() = AnimeShell(
    id, name, imageUrl, latestEpisode, status
)

fun Anime.asEntity() = AnimeEntity(
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
    lastModified = lastModified,
)