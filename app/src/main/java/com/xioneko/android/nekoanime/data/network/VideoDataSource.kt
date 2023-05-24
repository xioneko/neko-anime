package com.xioneko.android.nekoanime.data.network
import com.xioneko.android.nekoanime.data.model.Anime
import kotlinx.coroutines.flow.Flow

interface VideoDataSource {
    fun getVideoSource(anime: Anime, episode: Int): Flow<String>
}