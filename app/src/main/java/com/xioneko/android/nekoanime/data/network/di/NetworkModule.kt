package com.xioneko.android.nekoanime.data.network.di


import com.xioneko.android.nekoanime.data.network.util.ErrorHandlerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.IOException
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    val client = createHttpClient()
    private const val FAKE_BASE_URL = "http://www.example.com"
    private fun apiCreator(): Api {
        val retrofit = Retrofit.Builder()
            .baseUrl(FAKE_BASE_URL)
            .client(client)
            .build()
        return retrofit.create(Api::class.java)
    }

    suspend fun request(
        url: String,
        header: Map<String, String> = emptyMap()
    ): Response<ResponseBody> {
        return api.get(url, header)
    }

    private val api = apiCreator()
    @Provides
    @Singleton
    fun createHttpClient(): OkHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(ErrorHandlerInterceptor)
        .build()


    suspend fun getHtml(url: String, headers: Map<String, String> = emptyMap()): String {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).headers(headers.toHeaders()).get().build()
            val response = client.newCall(request).execute()
            var html: String
            if (response.isSuccessful) {
                response.body!!.let { body ->
                    html = body.charStream().readText()
                }
            } else {
                throw IOException(response.toString())
            }
            html
        }
    }


}

interface Api {

    @GET
    @Streaming
    suspend fun get(
        @Url url: String,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>
}