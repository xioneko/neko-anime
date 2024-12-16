package com.xioneko.android.nekoanime.data.network.danmu.dto

import kotlinx.serialization.Serializable

@Serializable
data class DanmuSearchResponse(
    val hasMore: Boolean = false,
    val animes: List<SearchAnimeEpisodes> = emptyList(),
    val errorCode: Int = 0,
    val success: Boolean = true,
    val errorMessage: String? = null
)

@Serializable
data class SearchAnimeEpisodes(
    val animeId: Int,
    val animeTitle: String,
    val type: String,
    val typeDescription: String,
    val episodes: List<SearchEpisodeDetails> = emptyList()
)

@Serializable
data class SearchEpisodeDetails(
    val episodeId: Int,
    val episodeTitle: String
)
