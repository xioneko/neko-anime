package com.xioneko.android.nekoanime.data

import com.xioneko.android.nekoanime.data.datastore.UserDataSource
import com.xioneko.android.nekoanime.data.model.SearchRecord
import com.xioneko.android.nekoanime.data.model.ThemeConfig
import com.xioneko.android.nekoanime.data.model.WatchRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataRepository @Inject constructor(
    private val userDataSource: UserDataSource,
) {
    val searchHistory: Flow<List<SearchRecord>> by userDataSource.userData

    val watchHistory: Flow<Map<Int, WatchRecord>> by userDataSource.userData

    val followedAnimeIds: Flow<List<Int>> by userDataSource.userData

    val themeConfig: Flow<ThemeConfig> by userDataSource.userData

    val updateAutoCheck: Flow<Boolean> by userDataSource.userData

    val disableLandscapeMode: Flow<Boolean> by userDataSource.userData

    val enablePortraitFullscreen: Flow<Boolean> by userDataSource.userData

    suspend fun addSearchRecord(record: String) =
        userDataSource.addSearchRecord(SearchRecord(record))

    suspend fun clearSearchRecord() = userDataSource.clearSearchRecord()


    suspend fun upsertWatchRecord(animeId: Int, episode: Int, positionMs: Long, percentageProgress: Int) {
        val positions = watchHistory.first()[animeId]?.positions?.toMutableMap() ?: mutableMapOf()
        val progress = watchHistory.first()[animeId]?.progress?.toMutableMap() ?: mutableMapOf()
        userDataSource.addWatchRecord(
            animeId,
            WatchRecord(Calendar.getInstance(), episode,
                positions.apply { put(episode, positionMs) },
                progress.apply { put(episode, percentageProgress) }
            )
        )
    }

    suspend fun clearWatchRecord() = userDataSource.clearWatchRecords()


    suspend fun addFollowedAnimeId(animeId: Int) =
        userDataSource.addFollowedAnimeId(animeId)

    suspend fun unfollowedAnime(animeId: Int) =
        userDataSource.unfollowedAnime(animeId)

    suspend fun setThemeConfig(config: ThemeConfig) =
        userDataSource.setThemeConfig(config)

    suspend fun setUpdateAutoCheck(enable: Boolean) =
        userDataSource.setUpdateAutoCheck(enable)

    suspend fun setDisableLandscapeMode(disable: Boolean) =
        userDataSource.setDisableLandscapeMode(disable)

    suspend fun setEnablePortraitFullscreen(enable: Boolean) =
        userDataSource.setEnablePortraitFullscreen(enable)
}