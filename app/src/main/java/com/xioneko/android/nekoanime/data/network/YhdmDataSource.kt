package com.xioneko.android.nekoanime.data.network

import android.util.Base64
import android.util.Log
import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.data.network.api.YhdmApi
import com.xioneko.android.nekoanime.data.network.api.YhdmPlayerApi
import com.xioneko.android.nekoanime.data.network.util.HtmlParser
import com.xioneko.android.nekoanime.data.network.util.JsoupConverterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.jsoup.nodes.Document
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.time.DayOfWeek
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class YhdmDataSource @Inject constructor(
    httpClient: OkHttpClient
) {
    private val yhdmApi = Retrofit.Builder()
        .client(httpClient)
        .baseUrl(YhdmApi.BASE_URL)
        .addConverterFactory(JsoupConverterFactory)
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(YhdmApi::class.java)

    private val yhdmPlayer = Retrofit.Builder()
        .client(httpClient)
        .baseUrl(YhdmPlayerApi.BASE_URL)
        .build()
        .create(YhdmPlayerApi::class.java)

    suspend fun getAnimeById(animeId: Int): Anime? {
        val document: Document = yhdmApi
            .getAnimeDetailPage(animeId)
            .body()
            ?: return null
        return HtmlParser.parseAnime(document, animeId)
    }

    fun getSearchResults(
        keyword: String,
        tag: String,
        actor: String,
        page: Int,
    ): Flow<List<AnimeShell>> = flow {
        yhdmApi
            .searchAnime(keyword, tag, actor, page)
            .also { if (!it.isSuccessful) throw HttpException(it) }
            .body()
            ?.let { document ->
                emit(HtmlParser.parseAnimeList(document))
            }
    }

    fun getSearchSuggests(
        keyword: String,
        limit: Int,
    ): Flow<List<String>> = flow {
        yhdmApi
            .getSearchSuggests(1, keyword, limit, System.currentTimeMillis())
            .also { if (!it.isSuccessful) throw HttpException(it) }
            .body()
            ?.let {
                emit(it.list.map { suggest -> suggest.name })
            }

    }

    fun getRetrievalResults(
        type: Int,
        orderBy: String,
        genre: String,
        year: String,
        letter: String,
        page: Int,
    ): Flow<List<AnimeShell>> = flow {
        yhdmApi.filterAnimeBy(type, orderBy, genre, year, letter, page)
            .also { if (!it.isSuccessful) throw HttpException(it) }
            .body()
            ?.let { document ->
                emit(HtmlParser.parseAnimeGrid(document))
            }
    }

    fun getWeeklyScheduleResults(): Flow<Map<DayOfWeek, List<AnimeShell>>> = flow {
        yhdmApi.getHomePage()
            .also { if (!it.isSuccessful) throw HttpException(it) }
            .body()
            ?.let {
                emit(HtmlParser.parseWeeklySchedule(it))
            }
    }

    suspend fun getVideoUrl(anime: Anime, episode: Int, streamId: Int): Pair<String, String?>? {
        val encryptedUrls = yhdmApi.getPlayPage(
            anime.id,
            episode,
            streamId
        )                // ^episode  ^episode + 1
            .takeIf { it.isSuccessful }
            ?.body()
            ?.let { document ->
                HtmlParser.parseEncryptedVideoUrl(document)
            } ?: return null

        val (url, nextUrl) = encryptedUrls
        val decryptedUrl = decryptUrl(url) ?: return null
        val decryptedNextUrl = nextUrl?.let { decryptUrl(it) }
        return decryptedUrl to decryptedNextUrl
    }

    private suspend fun decryptUrl(encryptedUrl: String): String? {
        try {
            val referrer =
                "${YhdmPlayerApi.BASE_URL}/player/index.php?code=qw&if=1&url=$encryptedUrl"
            val response = yhdmPlayer.getPlayerPage(encryptedUrl, referrer)
            val htmlText = response.string()

            val configUrl =
                Regex("\"url\"\\s*:\\s*(\"[^\"]*\")").find(htmlText)?.groupValues?.getOrNull(1)
                    ?.let { Json.decodeFromString<String>(it) } ?: return null
            val configUid =
                Regex("\"uid\"\\s*:\\s*(\"[^\"]*\")").find(htmlText)?.groupValues?.getOrNull(1)
                    ?.let { Json.decodeFromString<String>(it) } ?: return null

            val key = "2890${configUid}tB959C".toByteArray(Charsets.UTF_8)
            val iv = "2F131BE91247866E".toByteArray(Charsets.UTF_8)

            val secretKey = SecretKeySpec(key, "AES")
            val ivSpec = IvParameterSpec(iv)
            // https://stackoverflow.com/a/29234136/19594295
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

            val decrypted = cipher.doFinal(Base64.decode(configUrl, Base64.DEFAULT))
            return String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.d("Video,", "Decrypt failed: ${e.message}")
            return null
        }
    }
}