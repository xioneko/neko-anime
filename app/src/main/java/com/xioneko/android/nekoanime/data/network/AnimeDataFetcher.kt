package com.xioneko.android.nekoanime.data.network

import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeKey
import com.xioneko.android.nekoanime.data.network.datasource.YhdmDataSource
import com.xioneko.android.nekoanime.data.network.di.SourceHolder
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.FetcherResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnimeDataFetcher @Inject constructor() {
    val yhdmDataSource = YhdmDataSource
    val source = SourceHolder.currentSource
    operator fun invoke(): Fetcher<AnimeKey, Anime> =
        Fetcher.ofResult { key ->
            when (key) {
                is AnimeKey.FetchAnime -> {
                    source.getAnimeDetail(key.animeId)
//                    yhdmDataSource.getAnimeById(key.animeId)
                        .let {
                            if (it == null) {
                                FetcherResult.Error.Message("番剧信息获取失败: id=${key.animeId}")
                            } else {
                                FetcherResult.Data(it)
                            }
                        }
                }

                is AnimeKey.FetchVideo -> {
                    source.getVideoData(key.anime, key.episode, key.streamId)
                        //                    yhdmDataSource
                        //                        .getVideoUrl(key.anime, key.episode, key.streamId)
                        ?.let { (url, nextUrl) ->
                            FetcherResult.Data(
                                key.anime.copy(
                                    videoSource = key.anime.videoSource
                                        .toMutableMap()
                                        .apply {
                                            put(key.episode, url)
                                            nextUrl?.let {
                                                put(key.episode + 1, it)
                                            }
                                        }
                                )
                            )
                        }
                        ?: FetcherResult.Error.Message(
                            "视频地址获取失败: id=${key.anime.id}, episode=${
                                key.episode
                            }, sid=${key.streamId}"
                        )
                }
            }
        }
}