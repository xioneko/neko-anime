package com.xioneko.android.nekoanime.data.network.repository

import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeShell
import kotlinx.coroutines.flow.Flow

/**
 * 数据源统一转码
 */
interface AnimeSource {
    suspend fun getHomeData(): Flow<List<AnimeShell>>
    suspend fun getAnimeDetail(id: Int): Anime?
    suspend fun searchAnime(query: String, page: Int): List<AnimeShell>
    suspend fun getVideoData(anime: Anime, episode: Int, streamId: Int): Pair<String, String?>?

}