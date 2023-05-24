package com.xioneko.android.nekoanime.domain.model


data class FollowedAnime(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val latestEpisode: Int,
    val currentEpisode: Int,
    val isFinished: Boolean,
)