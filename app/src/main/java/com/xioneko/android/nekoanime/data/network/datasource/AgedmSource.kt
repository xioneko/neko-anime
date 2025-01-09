package com.xioneko.android.nekoanime.data.network.datasource

import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.data.model.model2.dto.VideoBean
import com.xioneko.android.nekoanime.data.network.api.AgedmApi
import com.xioneko.android.nekoanime.data.network.di.NetworkModule
import com.xioneko.android.nekoanime.data.network.repository.AnimeSource
import com.xioneko.android.nekoanime.data.network.util.HtmlParser
import com.xioneko.android.nekoanime.data.network.util.JsoupConverterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object AgedmSource : AnimeSource {
    private val agedmApi = Retrofit.Builder()
        .client(NetworkModule.createHttpClient())
        .baseUrl(AgedmApi.BASE_URL)
        .addConverterFactory(JsoupConverterFactory)
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(AgedmApi::class.java)

    override suspend fun getVideoData(episodeUrl: String): Flow<VideoBean> {
        TODO("Not yet implemented")
    }

    override suspend fun getAnimeDetail(animeId: Int): Flow<Anime> {
        TODO("Not yet implemented")
    }

    override suspend fun getHomeData(): Flow<List<AnimeShell>> = flow {
        //测试其他数据源数据 并解析
        agedmApi.getHomePage()
            .also { if (!it.isSuccessful) throw HttpException(it) }
            .body()
            ?.let { document ->
                emit(HtmlParser.parseAgedmHome(document))
            }
    }


}