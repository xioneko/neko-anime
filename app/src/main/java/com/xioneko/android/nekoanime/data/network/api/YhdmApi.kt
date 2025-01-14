package com.xioneko.android.nekoanime.data.network.api

import com.xioneko.android.nekoanime.data.network.model.SuggestsResponse
import org.jsoup.nodes.Document
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


/**
 * API示例:
 * 搜索：https://yhdm6.top/index.php/vod/search/?wd=异世界&page=3
 * 搜索建议：https://yhdm6.top/index.php/ajax/suggest?mid=1&wd=妹&limit=10&timestamp=1719555729692
 * 动漫详情：https://yhdm6.top/index.php/vod/detail/id/22287/
 * 分类检索：https://yhdm6.top/index.php/vod/show/?id=1&year=2022&class=&by=hits
 * 播放：https://yhdm6.top/index.php/vod/play/id/1911/sid/1/nid/12/
 */
interface YhdmApi {
    companion object {
        const val BASE_URL = "https://yhdm6.top"
    }

    @GET("/")
    suspend fun getHomePage(): Response<Document>

    @GET("/index.php/vod/search/")
    suspend fun searchAnime(
        @Query("wd") keyword: String,
        @Query("class") tag: String,
        @Query("actor") actor: String,
        @Query("page") page: Int,
    ): Response<Document>

    @GET("/index.php/ajax/suggest")
    suspend fun getSearchSuggests(
        @Query("mid") mid: Int,
        @Query("wd") keyword: String,
        @Query("limit") limit: Int,
        @Query("timestamp") timestamp: Long,
    ): Response<SuggestsResponse>

    @GET("/index.php/vod/detail/id/{animeId}/")
    suspend fun getAnimeDetailPage(@Path("animeId") animeId: Int): Response<Document>

    @GET("/index.php/vod/show/")
    suspend fun filterAnimeBy(
        @Query("id") type: Int?,
        @Query("by") orderBy: String?,
        @Query("class") genre: String?,
        @Query("year") year: String?,
        @Query("letter") letter: String?,
        @Query("page") page: Int?,
    ): Response<Document>

    @GET("/index.php/vod/play/id/{animeId}/sid/{sid}/nid/{nid}/")
    suspend fun getPlayPage(
        @Path("animeId") animeId: Int,
        @Path("nid") nid: Int,
        @Path("sid") sid: Int,
    ): Response<Document>

}