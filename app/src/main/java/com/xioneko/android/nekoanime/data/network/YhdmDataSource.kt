package com.xioneko.android.nekoanime.data.network

import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.data.network.api.YhdmApi
import com.xioneko.android.nekoanime.data.network.api.YhdmApi.Companion.BASE_URL
import com.xioneko.android.nekoanime.data.network.util.HtmlParser
import com.xioneko.android.nekoanime.data.network.util.JsoupConverterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.jsoup.nodes.Document
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.time.DayOfWeek
import javax.inject.Inject

class YhdmDataSource @Inject constructor(
    httpClient: OkHttpClient
) {
    private val yhdmApi = Retrofit.Builder()
        .client(httpClient)
        .baseUrl(BASE_URL)
        .addConverterFactory(JsoupConverterFactory)
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(YhdmApi::class.java)

    suspend fun getAnimeById(animeId: Int): Anime? {
        val document: Document = yhdmApi
            .getAnimeDetailPage(animeId)
            .body()
            ?: return null
        return HtmlParser.parseAnime(document, animeId)
    }

    fun getSearchResults(
        keyword: String,
        tag: String,
        actor: String,
        page: Int,
    ): Flow<List<AnimeShell>> = flow {
        yhdmApi
            .searchAnime(keyword, tag, actor, page)
            .also { if (!it.isSuccessful) throw HttpException(it) }
            .body()
            ?.let { document ->
                emit(HtmlParser.parseAnimeList(document))
            }
    }

    fun getSearchSuggests(
        keyword: String,
        limit: Int,
    ): Flow<List<String>> = flow {
        yhdmApi
            .getSearchSuggests(1, keyword, limit, System.currentTimeMillis())
            .also { if (!it.isSuccessful) throw HttpException(it) }
            .body()
            ?.let {
                emit(it.list.map { suggest -> suggest.name })
            }

    }

    fun getRetrievalResults(
        type: Int,
        orderBy: String,
        genre: String,
        year: String,
        letter: String,
        page: Int,
    ): Flow<List<AnimeShell>> = flow {
        yhdmApi.filterAnimeBy(type, orderBy, genre, year, letter, page)
            .also { if (!it.isSuccessful) throw HttpException(it) }
            .body()
            ?.let { document ->
                emit(HtmlParser.parseAnimeGrid(document))
            }
    }

    fun getWeeklyScheduleResults(): Flow<Map<DayOfWeek, List<AnimeShell>>> = flow {
        yhdmApi.getHomePage()
            .also { if (!it.isSuccessful) throw HttpException(it) }
            .body()
            ?.let {
                emit(HtmlParser.parseWeeklySchedule(it))
            }
    }

    suspend fun getVideoUrl(anime: Anime, episode: Int, streamId: Int): Pair<String, String?>? =
        yhdmApi.getPlayPage(anime.id, episode, streamId)                // ^episode  ^episode + 1
            .takeIf { it.isSuccessful }
            ?.body()
            ?.let { document ->
                HtmlParser.parseVideoUrl(document)
            }
}