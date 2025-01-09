package com.xioneko.android.nekoanime.data.network.di

import com.xioneko.android.nekoanime.NekoAnimeApplication
import com.xioneko.android.nekoanime.data.network.datasource.AgedmSource
import com.xioneko.android.nekoanime.data.network.datasource.YhdmDataSource
import com.xioneko.android.nekoanime.data.network.repository.AnimeSource
import com.xioneko.android.nekoanime.ui.util.KEY_SOURCE_MODE
import com.xioneko.android.nekoanime.ui.util.getEnum
import com.xioneko.android.nekoanime.ui.util.preferences
import javax.inject.Singleton

@Singleton
object SourceHolder {
    private lateinit var _currentSource: AnimeSource
    private lateinit var _currentSourceMode: NekoAnimeMode

    //获取用户数据源
    var DEFAULT_ANIME_SOURCE = NekoAnimeMode.Ydmi

    val currentSource: AnimeSource
        get() = _currentSource
    val currentSourceMode: NekoAnimeMode
        get() = _currentSourceMode

    var isSourceChanged = false

    init {
        val preferences = NekoAnimeApplication.getInstance().preferences
        initDefalutSource(preferences.getEnum(KEY_SOURCE_MODE, DEFAULT_ANIME_SOURCE))
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
            NekoAnimeMode.Ydmi -> YhdmDataSource
            NekoAnimeMode.Agedm -> AgedmSource
        }
    }


}

enum class NekoAnimeMode {
    Ydmi,
    Agedm
}