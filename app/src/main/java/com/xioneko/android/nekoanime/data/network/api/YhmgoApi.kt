package com.xioneko.android.nekoanime.data.network.api

import okhttp3.ResponseBody
import org.jsoup.nodes.Document
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import kotlin.random.Random

/**
 * API示例:
 * - 搜索：https://www.yhmgo.com/s_all?ex=1&kw=女神的露天咖啡厅
 * - 信息页：https://www.yhmgo.com/showp/22415.html
 * - 检索: https:/www.yhmgo.com/list/?region=日本&genre=TV&year=2022&season:=4&status=完结&label=运动&order=名称
 */
interface YhmgoApi {

    @GET("/")
    suspend fun getHomePage(): Response<Document>

    @GET("s_all")
    suspend fun searchAnime(
        @Query("kw") name: String,
        @Query("pageindex") pageIndex: Int,
    ): Response<Document>

    @GET("showp/{animeId}.html")
    suspend fun getAnimeDetailPage(@Path("animeId") animeId: Int): Response<Document>

    @GET("list/")
    suspend fun filterAnimeBy(
        @Query("region") region: String = "",
        @Query("genre") type: String = "",
        @Query("year") year: String = "",
        @Query("season") quarter: String = "",
        @Query("status") status: String = "",
        @Query("label") genre: String = "",
        @Query("order") orderBy: String = "",
        @Query("pageindex") pageIndex: Int = 0,
    ): Response<Document>

    @GET("playurl")
    suspend fun requestVideoUrl(
        @Query("aid") animeId: Int,
        @Query("playindex") channel: Int,
        @Query("epindex") epIndex: Int,
        @Query("r") rnd: Float = Random.nextFloat(),
        @Header("Referer") referrer: String,
        @Header("Cookie") cookies: String? = null
    ): Response<ResponseBody>
}
