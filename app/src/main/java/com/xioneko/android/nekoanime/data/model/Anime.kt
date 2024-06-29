package com.xioneko.android.nekoanime.data.model

import com.xioneko.android.nekoanime.data.db.model.AnimeEntity
import java.util.Calendar

data class Anime(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val status: String,
    val latestEpisode: Int,
    val tags: List<String> = emptyList(),
    val type: String,
    val year: String,
    val description: String,
    val streamIds: Set<Int>,
    val videoSource: Map<Int, String> = emptyMap(), // nid -> url
    val lastUpdate: Calendar
)

fun Anime.asAnimeShell() = AnimeShell(
    id, name, imageUrl, status
)

fun Anime.asEntity() = AnimeEntity(
    id = id,
    name = name,
    imageUrl = imageUrl,
    status = status,
    latestEpisode = latestEpisode,
    tags = tags,
    type = type,
    year = year,
    description = description,
    streamIds = streamIds,
    videoSource = videoSource,
    lastUpdate = lastUpdate,
)