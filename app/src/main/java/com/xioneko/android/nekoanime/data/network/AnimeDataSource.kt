package com.xioneko.android.nekoanime.data.network

import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.data.network.api.YhmgoApi
import com.xioneko.android.nekoanime.data.network.util.HtmlParser
import com.xioneko.android.nekoanime.data.network.util.JsoupConverterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import org.jsoup.nodes.Document
import retrofit2.Retrofit
import java.time.DayOfWeek
import javax.inject.Inject

class AnimeDataSource @Inject constructor(
    httpClient: OkHttpClient
) {
    companion object {
        const val BASE_URL = "https://www.yhmgo.com/"
    }

    private val yhmgoApi = Retrofit.Builder()
        .client(httpClient)
        .baseUrl(BASE_URL)
        .addConverterFactory(JsoupConverterFactory)
        .build()
        .create(YhmgoApi::class.java)

    suspend fun getAnimeById(animeId: Int): Anime? {
        val document: Document = yhmgoApi
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
        yhmgoApi
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
        yhmgoApi.filterAnimeBy(
            region, type, year, quarter, status, genre, orderBy, pageIndex
        )
            .takeIf { it.isSuccessful }
            ?.body()
            ?.let { document ->
                emit(HtmlParser.parseAnimeList(document))
            }
    }

    fun getWeeklyScheduleResults(): Flow<Map<DayOfWeek, List<AnimeShell>>> = flow {
        yhmgoApi.getHomePage()
            .takeIf { it.isSuccessful }
            ?.body()
            ?.let { document ->
                emit(HtmlParser.parseWeeklySchedule(document))
            }
    }

}


