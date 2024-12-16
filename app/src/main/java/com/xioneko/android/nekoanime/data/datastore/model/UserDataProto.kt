package com.xioneko.android.nekoanime.data.datastore.model

import com.xioneko.android.nekoanime.data.model.SearchRecord
import com.xioneko.android.nekoanime.data.model.ThemeConfig
import com.xioneko.android.nekoanime.data.model.WatchRecord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class UserDataProto @OptIn(ExperimentalSerializationApi::class) constructor(
    val searchRecords: List<SearchRecord> = emptyList(),
    val watchRecords: Map<Int, WatchRecord> = emptyMap(),
    @ProtoNumber(4)
    val followedAnimeIds: List<Int> = emptyList(),
    @ProtoNumber(6)
    val themeConfig: ThemeConfig = ThemeConfig.THEME_CONFIG_FOLLOW_SYSTEM,
    val updateAutoCheck: Boolean = true,
    val disableLandscapeMode: Boolean = true,
    val enablePortraitFullscreen: Boolean = false,
    val animeDataSource: String = "Ydmi"
)