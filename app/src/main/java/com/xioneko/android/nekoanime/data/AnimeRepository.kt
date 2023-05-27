package com.xioneko.android.nekoanime.data

import android.util.Log
import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeKey
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.data.network.AnimeDataSource
import com.xioneko.android.nekoanime.data.network.NetworkDataFetcher
import com.xioneko.android.nekoanime.data.network.VideoSourceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton


const val ANIME_LIST_PAGE_SIZE = 24

@Singleton
class AnimeRepository @Inject constructor(
    animeSourceOfTruth: AnimeSourceOfTruth,
    animeDataValidator: AnimeDataValidator,
    networkDataFetcher: NetworkDataFetcher,
    private val animeDataSource: AnimeDataSource,
    private val videoSourceManager: VideoSourceManager
) {
    private val store: Store<AnimeKey, Anime> =
        StoreBuilder.from<AnimeKey, Anime, Anime>(
            fetcher = networkDataFetcher(),
            sourceOfTruth = animeSourceOfTruth(),
        )
            .validator(animeDataValidator)
            .build()

    fun getAnimeById(
        animeId: Int,
        refresh: Boolean = false,
        skipMemory: Boolean = false
    ): Flow<Anime> = flow {
        store.stream(
            if (skipMemory) StoreReadRequest.skipMemory(AnimeKey.FetchAnime(animeId), refresh)
            else StoreReadRequest.cached(AnimeKey.FetchAnime(animeId), refresh)
        )
            .firstOrNull { it is StoreReadResponse.Data }
            ?.let { emit((it as StoreReadResponse.Data).value) }
    }


    fun getAnimeByName(
        animeName: String,
        pageIndex: Int,
        refresh: Boolean = false,
    ): Flow<Anime> =
        animeDataSource.getSearchResults(animeName, pageIndex)
            .mapNotNull {
                store.stream(StoreReadRequest.cached(AnimeKey.FetchAnime(it.id), refresh))
                    .firstOrNull { it is StoreReadResponse.Data }
                    .let { (it as StoreReadResponse.Data).value }
            }


    fun getRelativeAnime(animeName: String, pageIndex: Int = 0): Flow<String> =
        animeDataSource.getSearchResults(animeName, pageIndex)
            .map { it.name }


    fun getAnimeBy(
        region: String = "",
        type: String = "",
        year: String = "",
        quarter: String = "",
        status: String = "",
        genre: String = "",
        orderBy: String = "",
        pageIndex: Int,
    ): Flow<List<AnimeShell>> =
        animeDataSource
            .getRetrievalResults(region, type, year, quarter, status, genre, orderBy, pageIndex)


    fun getWeeklySchedule(): Flow<Map<DayOfWeek, List<AnimeShell>>> =
        animeDataSource.getWeeklyScheduleResults()


    fun getVideoUrl(
        anime: Anime,
        episode: Int,
        refresh: Boolean = false,
    ): Flow<List<String>> = flow {
        store.stream(StoreReadRequest.cached(AnimeKey.FetchVideo(anime, episode), refresh))
            .onEach { it.throwIfError() }
            .retry {
                Log.e("Video", it.message, it)
                Log.d("Video", "尝试下一个视频源")
                videoSourceManager.tryNext()
            }
            .catch {/* 尝试所有视频源后，如果依然无法解析到视频地址，则不发送任何 String */ }
            .firstOrNull { it is StoreReadResponse.Data }
            ?.let { emit((it as StoreReadResponse.Data).value.videoSource[episode]!!) }

    }
}