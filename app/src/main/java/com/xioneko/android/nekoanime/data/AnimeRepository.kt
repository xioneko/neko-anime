package com.xioneko.android.nekoanime.data

import androidx.media3.common.util.UnstableApi
import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeKey
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.data.network.AnimeDataFetcher
import com.xioneko.android.nekoanime.data.network.datasource.YhdmDataSource
import com.xioneko.android.nekoanime.data.network.di.SourceHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton


const val ANIME_LIST_PAGE_SIZE = 36
const val ANIME_GRID_PAGE_SIZE = 60

@Singleton
class AnimeRepository @androidx.annotation.OptIn(UnstableApi::class)
@Inject constructor(
    animeSourceOfTruth: AnimeSourceOfTruth,
    animeDataValidator: AnimeDataValidator,
    animeDataFetcher: AnimeDataFetcher
) {
    private val store: Store<AnimeKey, Anime> =
        StoreBuilder.from(
            fetcher = animeDataFetcher(),
            sourceOfTruth = animeSourceOfTruth(),
        )
            .validator(animeDataValidator)
            .build()

    val yhdmDataSource = YhdmDataSource

    //获取最近更新
    suspend fun getHomeDetail(): Flow<List<AnimeShell>> {
        val source = SourceHolder.currentSource
        return source.getHomeData()
    }

    //搜索
    suspend fun searchAnime(
        query: String,
        page: Int
    ): Flow<List<AnimeShell>> = flow {
        val source = SourceHolder.currentSource
        emit(source.searchAnime(query, page))
    }


    //详细动漫页
    fun getAnimeById(animeId: Int): Flow<Anime> = flow {
        store.stream(StoreReadRequest.cached(AnimeKey.FetchAnime(animeId), false))
            .firstOrNull { it is StoreReadResponse.Data }
            ?.let { emit((it as StoreReadResponse.Data).value) }
    }


    fun searchAnime(
        keyword: String = "",
        tag: String = "",
        actor: String = "",
        page: Int,
    ): Flow<List<AnimeShell>> =
        yhdmDataSource.getSearchResults(keyword, tag, actor, page)


    fun getSearchSuggests(keyword: String, limit: Int): Flow<List<String>> =
        yhdmDataSource.getSearchSuggests(keyword, limit)


    fun getAnimeBy(
        type: Int = 1,
        orderBy: String = "time",
        genre: String = "",
        year: String = "",
        letter: String = "",
        page: Int,
    ): Flow<List<AnimeShell>> =
        yhdmDataSource
            .getRetrievalResults(type, orderBy, genre, year, letter, page)


    fun getWeeklySchedule(): Flow<Map<DayOfWeek, List<AnimeShell>>> =
        yhdmDataSource.getWeeklyScheduleResults()


    fun getVideoUrl(
        anime: Anime,
        episode: Int,
        streamId: Int,
        fresh: Boolean = false,
    ): Flow<String> = flow {
        store.stream(
            if (fresh) StoreReadRequest.fresh(AnimeKey.FetchVideo(anime, episode, streamId))
            else StoreReadRequest.cached(AnimeKey.FetchVideo(anime, episode, streamId), false)
        )
            .firstOrNull { it is StoreReadResponse.Data }
            ?.let { emit((it as StoreReadResponse.Data).value.videoSource[episode]!!) }
    }

    suspend fun clearVideoSourceCache(anime: Anime, episode: Int, streamId: Int) =
        withContext(Dispatchers.IO) {
            store.clear(AnimeKey.FetchVideo(anime, episode, streamId))
        }

    @androidx.annotation.OptIn(UnstableApi::class)
    @OptIn(ExperimentalStoreApi::class)
    suspend fun clearAllCache() = withContext(Dispatchers.IO) {
        store.clear()
    }
}