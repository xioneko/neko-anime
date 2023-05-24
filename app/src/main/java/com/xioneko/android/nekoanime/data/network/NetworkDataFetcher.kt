package com.xioneko.android.nekoanime.data.network

import android.util.Log
import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeKey
import kotlinx.coroutines.flow.onEach
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
                    Log.d("Video", "准备解析${key.anime.name}的第${key.episode}话")
                    Log.d("Video", "采用视频源：${videoSourceManager.currentSource}")
                    videoSourceManager.currentSource
                        .getVideoSource(key.anime, key.episode)
                        .onEach { url ->
                            Log.d("Video", "解析得到视频地址<$url>")
                        }
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
                                    .Message("视频源解析失败, name=${key.anime.name}, episode=${key.episode}")
                            }
                        }
                }
            }
        }
}