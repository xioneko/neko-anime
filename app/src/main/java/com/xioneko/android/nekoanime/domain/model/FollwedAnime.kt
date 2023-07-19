package com.xioneko.android.nekoanime.domain.model


data class FollowedAnime(
    val id: Int,
    val name: String? = null,
    val imageUrl: String? = null,
    val latestEpisode: Int? = null,
    val currentEpisode: Int? = null,
    val isFinished: Boolean? = null,
)