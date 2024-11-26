package com.xioneko.android.nekoanime.data.network.api

import org.jsoup.nodes.Document
import retrofit2.Response
import retrofit2.http.GET

interface AgedmApi {
    companion object {
        const val BASE_URL = "https://www.agedm.org"
    }


    @GET("/")
    suspend fun getHomePage(): Response<Document>


}