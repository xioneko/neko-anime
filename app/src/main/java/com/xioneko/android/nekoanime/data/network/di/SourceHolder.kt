package com.xioneko.android.nekoanime.data.network.di

import com.xioneko.android.nekoanime.data.network.datasource.AgedmSource
import com.xioneko.android.nekoanime.data.network.datasource.YhdmDataSource
import com.xioneko.android.nekoanime.data.network.repository.AnimeSource
import com.xioneko.android.nekoanime.util.NekoAnimeMode
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceHolder @Inject constructor(
    private val httpClient: OkHttpClient
) {
    private lateinit var _currentSource: AnimeSource
    private lateinit var _currentSourceMode: NekoAnimeMode

    //获取用户数据源 TODO 初始化获取配置文件信息
    var DEFAULT_ANIME_SOURCE = NekoAnimeMode.Ydmi

    val currentSource: AnimeSource
        get() = _currentSource
    val currentSourceMode: NekoAnimeMode
        get() = _currentSourceMode

    init {
        initDefalutSource(DEFAULT_ANIME_SOURCE)
    }

    private fun initDefalutSource(mode: NekoAnimeMode) {
        _currentSource = getSource(mode)
        _currentSourceMode = mode
    }

    fun switchSource(mode: NekoAnimeMode) {
        _currentSource = getSource(mode)
        _currentSourceMode = mode
    }


    fun getSource(mode: NekoAnimeMode): AnimeSource {
        return when (mode) {
            NekoAnimeMode.Ydmi -> YhdmDataSource(httpClient)
            NekoAnimeMode.Agedm -> AgedmSource(httpClient)
        }
    }

    fun getSourceByName(mode: String): NekoAnimeMode {
        return when (mode) {
            "Ydmi" -> NekoAnimeMode.Ydmi
            "Agedm" -> NekoAnimeMode.Agedm
            else -> NekoAnimeMode.Agedm
        }
    }

}