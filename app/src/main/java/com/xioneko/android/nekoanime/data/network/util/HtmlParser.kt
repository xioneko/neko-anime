package com.xioneko.android.nekoanime.data.network.util

import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeShell
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.time.DayOfWeek
import java.util.Calendar

internal object HtmlParser {

    /**
     * 适配页面：
     * - https://www.yhdmz2.com/s_all
     * - https:/www.yhdmz2.com/list/
     */
    fun parseAnimeList(document: Document): List<AnimeShell> =
        buildList {
            val lis: Elements = document.select("div.lpic > ul > li")

            for (li in lis) {
                val id = li.child(0).attr("href")
                    .filter { it.isDigit() }
                    .toInt()
                val imgUrl = li.child(0).child(0).attr("src")
                    .let { if (it.startsWith("//")) "https:$it" else it }

                val name = li.child(1).child(0).ownText()

                val episodeInfo = li.child(2).child(0).ownText()

                val latestEpisode = episodeInfo.extractLatestEpisode()

                val status = episodeInfo.run {
                    when {
                        contains("完结|全集|-".toRegex()) -> "已完结"
                        contains("PV") -> "未播放"
                        isEmpty() -> "未知"
                        else -> "连载中"
                    }
                }

                add(
                    AnimeShell(
                        id = id,
                        name = name,
                        imageUrl = imgUrl,
                        latestEpisode = latestEpisode,
                        status = status
                    )
                )
            }
        }

    /**
     * 适配页面：
     * - https://www.yhdmz2.com/showp/{animeId}.html
     */
    fun parseAnime(document: Document, animeId: Int): Anime {
        val animeInfoElement: Element = document.select("div.rate.r").first()!!
        val div: Element = animeInfoElement.select(".sinfo").first()!!

        val name = animeInfoElement.select("h1").first()!!.ownText()
        val imageUrl = document.select("div.thumb.l > img").attr("src")
            .let { if (it.startsWith("//")) "https:$it" else it }

        val description = document.select("div.info").first()!!.ownText()

        val year: Int
        val release: String
        div.child(1).run {
            year = child(1).ownText().toInt()
            release = "" + year + ownText()
        }

        val genres = buildList<String> {
            div.child(3).select("a").forEach { add(it.ownText()) }
        }

        val status: String
        val type: String
        with(div.child(5).select("a")) {
            type = this[0].ownText()
            status = when (this[2].ownText()) {
                "连载" -> "连载中"
                "完结" -> "已完结"
                "未播放" -> "未播放"
                else -> "未知"
            }
        }

        val latestEpisode = div.child(6).ownText().extractLatestEpisode()

        return Anime(
            id = animeId,
            name = name,
            imageUrl = imageUrl,
            latestEpisode = latestEpisode,
            status = status,
            release = release,
            genres = genres,
            type = type,
            year = year,
            description = description,
            lastModified = Calendar.getInstance()
        )
    }

    /**
     * 适配页面：https://www.yhdmz2.com/
     */
    fun parseWeeklySchedule(document: Document): Map<DayOfWeek, List<AnimeShell>> = buildMap {
        document.select(("div.tlist")).first()!!.children()
            .forEachIndexed { index, ul -> // 0 -> 周一
                val animeShells = buildList {
                    ul.children().forEach { li ->
                        val span = li.child(0)
                        val a = li.child(1)
                        add(
                            AnimeShell(
                                id = a.attr("href")
                                    .filter { it.isDigit() }
                                    .toInt(),
                                name = a.attr("title"),
                                latestEpisode = span.child(0).ownText()
                                    .substringAfter("第")
                                    .filter { it.isDigit() }.toInt(),
                                status = if (span.child(0).ownText().contains("完结"))
                                    "已完结" else "连载中",
                            )
                        )
                    }
                }
                put(DayOfWeek.of(index + 1), animeShells)
            }
    }

    /**
     * 适配页面：
     * - http://www.yinghuavideo.com/v/{animeId}-{episode}.html
     */
    fun parseVideoSource(document: Document): String =
        document.select("#playbox").first()!!
            .attr("data-vid")
            .substringBefore("\$")

    /**
     * 适配：
     * 第24集(完结) \ 22:00 第2集(每周一22:00更新)
     * 第1集(每周一更新) \ [OVA 01-04] \[全集]
     * 第3话(完结) \ 第OVA1话 \ [OVA 01-02+SP]
     * 第06集(完结) \ [TV 01-12+SP01-06]
     */
    private fun String.extractLatestEpisode() =
        this.substringAfter('第')
            .substringBefore('+')
            .substringAfter('-')
            .substringBefore('(')
            .filter { it.isDigit() }
            .takeIf { it.isNotEmpty() }
            ?.toInt()
            ?: 1


}