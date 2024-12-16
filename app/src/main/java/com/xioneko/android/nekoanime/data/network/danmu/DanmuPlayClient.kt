package com.xioneko.android.nekoanime.data.network.danmu

import com.xioneko.android.nekoanime.data.network.api.DanmuApi
import com.xioneko.android.nekoanime.data.network.api.DanmuApi.Companion.BASE_URL
import com.xioneko.android.nekoanime.data.network.danmu.dto.DanmuInfo
import com.xioneko.android.nekoanime.data.network.danmu.dto.DanmuSearchResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class DanmuPlayClient @Inject constructor(
    private val client: OkHttpClient
) {
    val danApi = Retrofit.Builder()
        .client(client)
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DanmuApi::class.java)


    suspend fun searchEpisode(
        subjectName: String,
        episodeName: String?
    ): DanmuSearchResponse? {
        return danApi.SearchEpisode(subjectName, episodeName).body()
    }


    suspend fun getDanmuList(
        episodeId: Long
    ): List<DanmuInfo> {
        val response = danApi.getDanmuList(episodeId).body()
        return response?.comments ?: emptyList()
    }

}