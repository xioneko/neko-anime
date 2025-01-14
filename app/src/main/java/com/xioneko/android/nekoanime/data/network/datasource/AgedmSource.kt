package com.xioneko.android.nekoanime.data.network.datasource

import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.data.model.model2.dto.EpisodeBean
import com.xioneko.android.nekoanime.data.network.di.NetworkModule
import com.xioneko.android.nekoanime.data.network.repository.AnimeSource
import com.xioneko.android.nekoanime.data.network.util.HtmlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okhttp3.internal.closeQuietly
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import retrofit2.Response
import java.net.URI
import java.util.Calendar

object AgedmSource : AnimeSource {
//    private val agedmApi = Retrofit.Builder()
//        .client(NetworkModule.createHttpClient())
//        .baseUrl(AgedmApi.BASE_URL)
//        .addConverterFactory(JsoupConverterFactory)
//        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
//        .build()
//        .create(AgedmApi::class.java)

    const val BASE_URL = "https://www.agedm.org"
    private const val LOG_TAG = "AgedmSource"
    private val webViewUtil: WebViewUtil by lazy { WebViewUtil() }

    private val filterReqUrl: Array<String> = arrayOf(
        ".css", ".js", ".jpeg", ".svg", ".ico", ".ts",
        ".gif", ".jpg", ".png", ".webp", ".wasm", "age", ".php"
    )


    override suspend fun getVideoData(
        anime: Anime,
        episode: Int,
        streamId: Int
    ): Pair<String, String?> {
        val html1 = NetworkModule.getHtml("$BASE_URL/play/${anime.id}/$streamId/$episode")
        val document = Jsoup.parse(html1)
        val videoUrl1 = getVideoUrl(document)
        var videoUrl2: String = ""
        if (episode != anime.latestEpisode) {
            val html2 = NetworkModule.getHtml("$BASE_URL/play/${anime.id}/$streamId/${episode + 1}")
            videoUrl2 = getVideoUrl(Jsoup.parse(html2))
        }
        return videoUrl1 to (videoUrl2.takeIf { it.isNotEmpty() })
    }

    override suspend fun searchAnime(query: String, page: Int): List<AnimeShell> {
        val html = NetworkModule.getHtml("$BASE_URL/search?query=$query&page=$page")
        val document = Jsoup.parse(html)
        val animeList = mutableListOf<AnimeShell>()
        document.select("div.card").forEach { el ->
            val title = el.select("h5").text()
            val url = el.select("h5 > a").attr("href")
            val imgUrl = el.select("img").attr("data-original")
            val id = URI(url).path.split("/").last()
            val status = el.select("video_play_status").text()
            animeList.add(
                AnimeShell(
                    id = id.toInt(),
                    name = title,
                    imageUrl = imgUrl,
                    status = status
                )
            )
        }
        return animeList
    }

    override suspend fun getAnimeDetail(id: Int): Anime? {
        val html = NetworkModule.getHtml("$BASE_URL/detail/$id")
        val document = Jsoup.parse(html)
        val videoDetailRight = document.select("div.video_detail_right")
        val title = videoDetailRight.select("h2").text()
        val desc = videoDetailRight.select("div.video_detail_desc").text()
        val imgUrl = document.select("div.video_detail_cover > img").attr("data-original")
        val detailBoxList = document.select("div.video_detail_box").select("li")
        val tags = detailBoxList[9].text().split("：")[1].split(" ").toMutableList()
        tags.add(detailBoxList[0].text().split("：")[1])
        tags.add(detailBoxList[1].text().split("：")[1])
        //播放状态
        val status = detailBoxList[7].text().split("：")[1]
        val playlist = document.select("div.tab-content").select("div.tab-pane")
        val channels = getAnimeEpisodes(playlist)
//        val relatedAnimes =
//            getAnimeList(document.select("div.video_list_box").select("div.video_item"))
        val episodeBeans = channels.get(0)
        //多线路ID
        val streamIds = mutableSetOf<Int>()
        streamIds.addAll(1..channels.size)
        return Anime(
            id = id,
            name = title,
            imageUrl = imgUrl,
            status = status,
            latestEpisode = episodeBeans!!.size,
            tags = tags,
            type = detailBoxList[1].text().split("：")[1],
            year = detailBoxList[6].text().split("：")[1],
            description = desc,
            streamIds = streamIds,
            lastUpdate = Calendar.getInstance()
        )

    }

    override suspend fun getHomeData(): Flow<List<AnimeShell>> = flow {
        //测试其他数据源数据 并解析
        val html = NetworkModule.getHtml(BASE_URL)
        val document = Jsoup.parse(html)
        emit(HtmlParser.parseAgedmHome(document))
    }

    private suspend fun getVideoUrl(document: Document): String {
        val videoUrl = document.select("#iframeForVideo").attr("src")

        // 用于判断url的返回类型是否是 video/mp4
        val predicate: suspend (requestUrl: String) -> Boolean = { requestUrl ->
            withContext(Dispatchers.IO) {
                var response: Response<ResponseBody>? = null
                try {
                    response = NetworkModule.request(requestUrl)
                    response.isSuccessful && response.isVideoType()
                } catch (_: Exception) {
                    false
                } finally {
                    response?.closeQuietly()
                }
            }
        }

        return webViewUtil.interceptRequest(
            url = videoUrl,
            regex = ".mp4|.m3u8|video|playurl|hsl|obj|bili",
            predicate = predicate,
            filterRequestUrl = filterReqUrl
        )
    }

    private fun Response<*>.isVideoType(): Boolean {
        return header("Content-Type") == "video/mp4"
    }

    private fun Response<*>.header(key: String): String {
        val header = headers()[key]
        return header ?: ""
    }

    private fun Response<ResponseBody>.closeQuietly() {
        body()?.closeQuietly()
        errorBody()?.closeQuietly()
    }
}

private suspend fun getAnimeEpisodes(elements: Elements): Map<Int, List<EpisodeBean>> {
    val channels = mutableMapOf<Int, List<EpisodeBean>>()


    elements.forEachIndexed { i, e ->
        val episodes = mutableListOf<EpisodeBean>()
        e.select("li").forEach { el ->
            val name = el.text()
            val url = el.select("a").attr("href")
            episodes.add(EpisodeBean(name, url))
        }
        channels[i] = episodes
    }

    return channels
}

private suspend fun getAnimeList(elements: Elements): List<Anime> {
    val animeList = mutableListOf<Anime>()
//    elements.forEach { el ->
//        val title = el.select("a").text()
//        val url = el.select("a").attr("href")
//        val imgUrl = el.select("img").attr("data-original")
//        val episodeName = el.select("span.video_item--info").text()
//    }
    return animeList
}