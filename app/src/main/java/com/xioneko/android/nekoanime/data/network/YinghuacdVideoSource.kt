package com.xioneko.android.nekoanime.data.network

import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.network.api.VideoSourceApi
import com.xioneko.android.nekoanime.data.network.util.HtmlParser
import com.xioneko.android.nekoanime.data.network.util.JsoupConverterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import org.jsoup.nodes.Document
import retrofit2.Retrofit
import javax.inject.Inject

private const val BASE_URL = "http://www.yinghuacd.com/"

class YinghuacdVideoSource @Inject constructor(
    httpClient: OkHttpClient
) : VideoDataSource {
    private val videoSourceApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(JsoupConverterFactory)
        .build()
        .create(VideoSourceApi::class.java)

    override fun getVideoSource(anime: Anime, episode: Int): Flow<String> = flow {
        val document: Document? = videoSourceApi
            .searchAnime(anime.name)
            .takeIf { it.isSuccessful }
            ?.body()

        if (document != null)
            HtmlParser.parseAnimeList(document).let { animeList ->
                if (animeList.size == 1) {
                    val animeShell = animeList.first()
                    val videoDocument: Document? = videoSourceApi
                        .getAnimeVideoPage(animeShell.id, episode)
                        .takeIf { it.isSuccessful }
                        ?.body()

                    if (videoDocument != null)
                        HtmlParser.parseVideoSource(videoDocument)
                            .takeIf { it.endsWith("m3u8") }
                            ?.let { emit(it) }
                }
            }
    }
    override fun toString(): String = "动漫视频源<$BASE_URL>"
}