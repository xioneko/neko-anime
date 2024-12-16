package com.xioneko.android.nekoanime.data.network.api

import com.xioneko.android.nekoanime.data.network.danmu.dto.DanmuInfoListResponse
import com.xioneko.android.nekoanime.data.network.danmu.dto.DanmuSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface DanmuApi {
    companion object {
        const val BASE_URL = "https://api.dandanplay.net/api/v2/"
    }

    @GET("search/episodes")
    @Headers("Content-Type:application/json; charset=utf-8")
    suspend fun SearchEpisode(
        @Query(value = "anime") subjectName: String,
        @Query(value = "episode") episodeName: String?
    ): Response<DanmuSearchResponse>

    @GET("comment/{episodeId}?chConvert=0&withRelated=true")
    suspend fun getDanmuList(
        @Path("episodeId") episodeId: Long
    ): Response<DanmuInfoListResponse>


}