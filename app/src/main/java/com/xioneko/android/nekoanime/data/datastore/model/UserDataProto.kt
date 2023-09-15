package com.xioneko.android.nekoanime.data.datastore.model

import com.xioneko.android.nekoanime.data.model.DownloadRecord
import com.xioneko.android.nekoanime.data.model.SearchRecord
import com.xioneko.android.nekoanime.data.model.ThemeConfig
import com.xioneko.android.nekoanime.data.model.WatchRecord
import kotlinx.serialization.Serializable

@Serializable
data class UserDataProto(
    val searchRecords: List<SearchRecord> = emptyList(),
    val watchRecords: Map<Int, WatchRecord> = emptyMap(),
    val downloadRecords: Map<Int, DownloadRecord> = emptyMap(),
    val followedAnimeIds: List<Int> = emptyList(),
    val interests: List<String> = emptyList(),
    val themeConfig: ThemeConfig = ThemeConfig.THEME_CONFIG_FOLLOW_SYSTEM,
    val updateAutoCheck: Boolean = true,
    val disableLandscapeMode: Boolean = true,
)