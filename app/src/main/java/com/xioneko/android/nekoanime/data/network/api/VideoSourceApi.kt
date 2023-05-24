package com.xioneko.android.nekoanime.data.network.api

import org.jsoup.nodes.Document
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path


/**
 * API示例:
 * - 搜索：http://www.yinghuacd.com/search/女神的露天咖啡厅
 * - 视频页：http://www.yinghuacd.com/v/5879-1.html
 */
internal interface VideoSourceApi {
    @GET("search/{name}")
    suspend fun searchAnime(@Path("name") name: String): Response<Document>

    @GET("v/{animeId}-{episode}.html")
    suspend fun getAnimeVideoPage(
        @Path("animeId") animeId: Int,
        @Path("episode") episode: Int
    ): Response<Document>
}