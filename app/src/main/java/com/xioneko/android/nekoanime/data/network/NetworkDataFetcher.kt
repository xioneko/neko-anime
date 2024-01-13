package com.xioneko.android.nekoanime.data.network

import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeKey
import kotlinx.coroutines.flow.toList
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.FetcherResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkDataFetcher @Inject constructor(
    private val animeDataSource: AnimeDataSource,
    private val videoSourceManager: VideoSourceManager,
) {

    operator fun invoke(): Fetcher<AnimeKey, Anime> =
        Fetcher.ofResult { key ->
            when (key) {
                is AnimeKey.FetchAnime -> {
                    animeDataSource.getAnimeById(key.animeId).let {
                        if (it == null) {
                            FetcherResult.Error.Message("番剧信息获取失败, id=${key.animeId}")
                        } else {
                            FetcherResult.Data(it)
                        }
                    }
                }

                is AnimeKey.FetchVideo -> {
                    videoSourceManager.currentSource
                        .getVideoSource(key.anime, key.episode)
                        .toList()
                        .let { urls ->
                            if (urls.isNotEmpty()) {
                                FetcherResult.Data(
                                    key.anime.copy(
                                        videoSource = key.anime.videoSource
                                            .toMutableMap()
                                            .apply { put(key.episode, urls) }
                                    )
                                )
                            } else {
                                FetcherResult.Error
                                    .Message("视频地址获取失败<${key.anime.name}><ep${key.episode}>")
                            }
                        }
                }
            }
        }
}