package com.xioneko.android.nekoanime.data.network.util

import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeShell
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URLDecoder
import java.time.DayOfWeek
import java.util.Calendar

internal object HtmlParser {

    /**
     * 适配页面：https://yhdm6.top/index.php/vod/search/?wd={keyword}&page={page}
     */
    fun parseAnimeList(document: Document): List<AnimeShell> = buildList {
        for (li in document.select("li.searchlist_item")) {
            val a = li.selectFirst(".searchlist_img > a")!!
            add(
                AnimeShell(
                    id = a.attr("href")
                        .substringAfter("id/")
                        .substringBefore("/")
                        .toInt(),
                    name = a.attr("title"),
                    imageUrl = a.attr("data-original"),
                    status = a.lastElementChild()!!.ownText()
                )
            )
        }
    }

    /**
     * 适配页面：https://yhdm6.top/index.php/vod/show/?id={type}&year={year}&class={genre}&by={orderBy}&page={page}
     */
    fun parseAnimeGrid(document: Document): List<AnimeShell> = buildList {
        for (li in document.select(".vodlist_wi > .vodlist_item")) {
            val a = li.child(0)
            add(
                AnimeShell(
                    id = a.attr("href")
                        .substringAfter("id/")
                        .substringBefore("/")
                        .toInt(),
                    name = a.attr("title"),
                    imageUrl = a.attr("data-original"),
                    status = a.lastElementChild()!!.ownText()
                )
            )
        }
    }

    /**
     * 解析https://www.agedm.org首页
     */
    fun parseAgedmHome(document: Document): List<AnimeShell> = buildList {
        document.select("div.container").select("div.video_list_box")
            .forEach {
                val title = it.select("h6").text().replace("更多 »", "")
                if ("最近更新".equals(title)) {
                    it.select("div.video_item")
                        .forEach {
                            val title = it.select("a").text()
                            val id = it.select("a").attr("href")
                                .substringAfter("detail/")
                                .toInt()
                            val imgUrl = it.select("img").attr("data-original")
                            //集数
                            val episodeName = it.select("span.video_item--info").text()
                            add(
                                AnimeShell(
                                    id = id,
                                    name = title,
                                    imageUrl = imgUrl,
                                    status = episodeName
                                )
                            )
                        }
                }
            }
    }



    /**
     * 适配页面：https://yhdm6.top/index.php/vod/detail/id/{animeId}/
     */
    fun parseAnime(document: Document, animeId: Int): Anime {
        val imageUrl = document.selectFirst(".content_thumb > a")!!.attr("data-original")
        val name = document.selectFirst(".content_detail h2")!!.ownText()

        val lis = document.select(".content_detail li.data")
        val year = lis[0].selectFirst("span:contains(年份)")!!.nextElementSibling()!!.ownText()
        val tags =
            lis[0].selectFirst("span:contains(类型)")!!.nextElementSiblings().map(Element::ownText)
        val status = lis[1].selectFirst("span:contains(状态)")!!.nextElementSibling()!!.ownText()
        val description = document.selectFirst(".content .full_text > span")!!.ownText()
        val type = document.selectFirst("ul.top_nav > li.active")!!.text()

        val playList = document.select("div.playlist_full")
        var latestEpisode: Int? = null
        val streamIds = mutableSetOf<Int>()
        for (div in playList) {
            val a = div.selectFirst("a") ?: continue
            if (type != "动漫电影" && !a.ownText().startsWith("第")) continue
            val sid = a.attr("href")
                .substringAfter("sid/")
                .substringBefore("/")
                .toInt()
            streamIds.add(sid)
            if (latestEpisode == null) {
                latestEpisode = if (type == "动漫电影") 1 else div.select("a").size
            }
        }

        return Anime(
            id = animeId,
            name = name,
            imageUrl = imageUrl,
            status = status,
            latestEpisode = latestEpisode ?: 0,
            tags = tags,
            type = type.takeIfNotEmptyOr("未知"),
            year = year.takeIfNotEmptyOr("未知"),
            description = description,
            streamIds = streamIds,
            lastUpdate = Calendar.getInstance()
        )
    }

    /**
     * 适配页面：https://yhdm6.top/
     */
    fun parseWeeklySchedule(document: Document): Map<DayOfWeek, List<AnimeShell>> = buildMap {
        document.select(("div.container > div:nth-child(2) ul"))
            .forEachIndexed { index, ul -> // 0 -> 周一
                val animeShells = buildList {
                    for (li in ul.children()) {
                        val a = li.child(0)
                        add(
                            AnimeShell(
                                id = a.attr("href")
                                    .substringAfter("id/")
                                    .substringBefore("/")
                                    .toInt(),
                                name = a.attr("title"),
                                status = a.lastElementChild()!!.ownText()
                            )
                        )
                    }
                }
                put(DayOfWeek.of(index + 1), animeShells)
            }
    }

    /**
     * 适配页面：https://yhdm6.top/index.php/vod/play/id/{animeId}/sid/{streamId}/nid/{episode}/
     */
    fun parseVideoUrl(document: Document): Pair<String, String?>? =
        document.selectFirst(".player_video script")!!.html()
            .let { code ->
//                Log.d("Video", "script: $code")
                Regex("""url"\s*:\s*"([^"]*)".*"url_next"\s*:\s*"([^"]*)""")
                    .find(code)?.groupValues
                    ?.map { URLDecoder.decode(it, "UTF-8") }
                    ?.let { urls ->
                        urls[1] to (urls[2].takeIf { it.isNotEmpty() })
                    }
            }
}

private fun String.takeIfNotEmptyOr(default: String) = takeIf { it.isNotEmpty() } ?: default