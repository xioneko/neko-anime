package com.xioneko.android.nekoanime.data.network

import android.util.Log
import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.network.api.YhmgoApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.net.URLDecoder
import javax.inject.Inject

class YhmgoVideoSource @Inject constructor(
    httpClient: OkHttpClient,
) : VideoDataSource {

    companion object {
        const val BASE_URL = "https://www.yhmgo.com"
    }

    private val yhmgo = Retrofit.Builder()
        .client(httpClient)
        .baseUrl(BASE_URL)
        .build()
        .create(YhmgoApi::class.java)

    /**
     * Inspired by @hehe1005566889
     *
     * Based on:
     * - https://github.com/xioneko/neko-anime/issues/12#issue-2060922443
     * - https://www.yhmgo.com/tpsf/js/pck.js?ver=215139
     */
    override fun getVideoSource(anime: Anime, episode: Int) = flow {
        val channels = listOf(1, 2)

        channels.forEach { channel ->
            val maxTry = 4
            var cookies = ""

            repeat(maxTry) {
                val res = yhmgo.requestVideoUrl(
                    animeId = anime.id,
                    channel = channel,
                    epIndex = episode - 1,
                    referrer = "$BASE_URL/vp/${anime.id}-$channel-${episode - 1}",
                    cookies = cookies
                )
                val body = res.body()?.string()

                if (res.isSuccessful && body != null) {
                    if (body.contains("not verified")) {
                        val c = res.headers().values("set-cookie").joinToString()
                        val r = Regex("t1=(\\d+).*k1=(\\d+)").find(c)?.groupValues
                        val t1 = r?.get(1)
                        val k1 = r?.get(2)

                        if (t1 != null && k1 != null) {
                            val m2t = calculateM2t()
                            val k2 = calculateK2(t1)
                            val t2 = calculateT2(k2.toString())

                            cookies = buildString {
                                append("t1=$t1; ")
                                append("t2=$t2; ")
                                append("k1=$k1; ")
                                append("k2=$k2; ")
                                append("m2t=$m2t")
                            }
//                            Log.d("Video", "Cookies: $cookies")
                        }
                    } else {
                        val videoUrl = parseUrl(body)
                        Log.d(
                            "Video",
                            "解析得到视频地址<${anime.name}><ep$episode><ch$channel>: $videoUrl"
                        )

                        emit(videoUrl)
                        return@forEach
                    }
                }
                delay(500)
            }

            Log.d("Video", "获取视频地址失败<${anime.name}><ep$episode><ch$channel>")
        }
    }

    private fun calculateM2t(): Long {
        val ts = (System.currentTimeMillis() / 0x3e8) shr 19
        return (ts * 0x15 + 0x9a) *
                (ts % 0x40 + 0xd) *
                (ts % 0x20 + 0x22) *
                (ts % 0x10 + 0x57) *
                (ts % 0x8 + 0x41) + 0x2ef
    }

    private fun calculateK2(t1: String): Long {
        val ts = (t1.toLong() / 0x3e8) shr 0x5
        return (ts * (ts % 0x100 + 0x1) + 0x89a4) *
                (ts % 0x80 + 0x1) *
                (ts % 0x10 + 0x1) + ts
    }

    private fun calculateT2(k2: String): String {
        while (true) {
            val t2 = System.currentTimeMillis().toString()
            val a = t2.substring(t2.length - 3)
            val b = k2.substring(k2.length - 1)
            if (a.contains(b)) return t2
        }
    }

    private fun parseUrl(data: String): String {
        val json = data
            .chunked(2)
            .foldIndexed("") { i, acc, cc ->
                var code = cc.toInt(16)
                code = (code + 0x100000 - 0x619 - (data.length / 2 - i - 1)) % 0x100
                code.toChar() + acc
            }

        val info = Json.parseToJsonElement(json).jsonObject

        return URLDecoder.decode(info["vurl"].toString(), "UTF-8").trim('"')
    }

    override fun toString(): String = "动漫视频源<$BASE_URL>"
}