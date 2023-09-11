package com.xioneko.android.nekoanime.data.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import com.xioneko.android.nekoanime.data.model.Anime
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import java.net.URLDecoder
import javax.inject.Inject

class YhmgoVideoSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : VideoDataSource {

    companion object {
        const val BASE_URL = "https://www.yhmgo.com/"
    }

    private var webView: WebView? = null

    private val channels = listOf(1, 2)

    override fun getVideoSource(anime: Anime, episode: Int): Flow<String> =
        tryFetchVideoUrl(anime.id, episode)
            .filter { it.endsWith("m3u8") }

    @SuppressLint("SetJavaScriptEnabled")
    private fun tryFetchVideoUrl(animeId: Int, episode: Int) = channelFlow {
        if (webView == null) {
            webView = WebView(context).apply { settings.javaScriptEnabled = true }
        }

        webView!!.run {
            Log.d("Video", "运行 WebView")
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    parseAndSend(view, url)
                }
            }.apply {
                settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/114.0.0.0 Safari/537.36 Edg/114.0.1823.37"
            }
            Log.d("Video", "开始加载页面")
            loadUrl("${BASE_URL}vp/${animeId}-${channels.first()}-${episode - 1}.html")
        }

        awaitClose {}

    }.flowOn(Dispatchers.Main)


    private fun ProducerScope<String>.parseAndSend(view: WebView, url: String) {
        val channel = url.substringAfter('-').substringBefore('-').toInt()

        view.evaluateJavascript(
            "document.querySelector('#yh_playfram').src"
        ) { it ->
            val frameSrc = it.trim('"')
            Log.d("Video", "解析得到src<$frameSrc>")

            if (frameSrc == "null") {
                Log.d("Video", "页面加载失败<$url>")
            } else {
                if (frameSrc == url) {
                    parseAndSend(view, url).also { Log.d("Video", "重新尝试解析src") }
                    return@evaluateJavascript
                }
                trySend(
                    frameSrc.substringAfter("&url=")
                        .substringBefore("&")
                        .let { URLDecoder.decode(it, "UTF-8") }
                        .also { Log.d("Video", "解析得到视频地址<$it>") }
                )
            }

            if (channel == channels.last())
                close()
            else
                view.loadUrl(
                    url.replaceFirst(
                        oldValue = "-$channel-",
                        newValue = "-${channels[channels.indexOf(channel) + 1]}-"
                    )
                )
        }
    }

    override fun toString(): String = "动漫视频源<$BASE_URL>"
}