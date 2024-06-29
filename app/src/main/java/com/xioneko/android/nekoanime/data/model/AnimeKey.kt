package com.xioneko.android.nekoanime.data.model

sealed class AnimeKey {
    data class FetchAnime(val animeId: Int): AnimeKey()
    data class FetchVideo(val anime: Anime, val episode: Int, val streamId: Int) : AnimeKey()
}