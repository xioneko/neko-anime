package com.xioneko.android.nekoanime.data.network.repository

import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.data.model.model2.dto.VideoBean
import kotlinx.coroutines.flow.Flow

/**
 * 数据源统一转码
 */
interface AnimeSource {
    //    val KEY_SOURCE_DOMAIN: String
//        get() = "${this.javaClass.simpleName}Domain"
//    val DEFAULT_DOMAIN: String
//    val baseUrl:String
    suspend fun getHomeData(): Flow<List<AnimeShell>>
    suspend fun getAnimeDetail(animeId: Int): Flow<Anime>
    suspend fun getVideoData(episodeUrl: String): Flow<VideoBean>

}