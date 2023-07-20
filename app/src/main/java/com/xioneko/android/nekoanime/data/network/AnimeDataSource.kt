package com.xioneko.android.nekoanime.data.network

import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.data.network.api.AnimeRetrievalApi
import com.xioneko.android.nekoanime.data.network.util.HtmlParser
import com.xioneko.android.nekoanime.data.network.util.JsoupConverterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import org.jsoup.nodes.Document
import retrofit2.Retrofit
import java.time.DayOfWeek
import javax.inject.Inject

private const val BASE_URL = "https://www.yhdmzz.com/"


class AnimeDataSource @Inject constructor(
    httpClient: OkHttpClient
) {
    private val animeRetrievalApi = Retrofit.Builder()
        .client(httpClient)
        .baseUrl(BASE_URL)
        .addConverterFactory(JsoupConverterFactory)
        .build()
        .create(AnimeRetrievalApi::class.java)

    suspend fun getAnimeById(animeId: Int): Anime? {
        val document: Document = animeRetrievalApi
            .getAnimeDetailPage(animeId)
            .takeIf { it.isSuccessful }
            ?.body()
            ?: return null
        return HtmlParser.parseAnime(document, animeId)
    }


    fun getSearchResults(
        animeName: String,
        pageIndex: Int = 0,
    ): Flow<AnimeShell> = flow {
        animeRetrievalApi
            .searchAnime(animeName, pageIndex)
            .takeIf { it.isSuccessful }
            ?.body()
            ?.let { document ->
                HtmlParser.parseAnimeList(document).forEach { emit(it) }
            }
    }


    fun getRetrievalResults(
        region: String = "",
        type: String = "",
        year: String = "",
        quarter: String = "",
        status: String = "",
        genre: String = "",
        orderBy: String = "",
        pageIndex: Int = 0,
    ): Flow<List<AnimeShell>> = flow {
        animeRetrievalApi.filterAnimeBy(
            region, type, year, quarter, status, genre, orderBy, pageIndex
        )
            .takeIf { it.isSuccessful }
            ?.body()
            ?.let { document ->
                emit(HtmlParser.parseAnimeList(document))
            }
    }

    fun getWeeklyScheduleResults(): Flow<Map<DayOfWeek, List<AnimeShell>>> = flow {
        animeRetrievalApi.getHomePage()
            .takeIf { it.isSuccessful }
            ?.body()
            ?.let { document ->
                emit(HtmlParser.parseWeeklySchedule(document))
            }
    }

}


