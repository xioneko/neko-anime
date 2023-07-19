package com.xioneko.android.nekoanime.data.datastore

import androidx.datastore.core.DataStore
import com.xioneko.android.nekoanime.data.datastore.model.UserDataProto
import com.xioneko.android.nekoanime.data.model.DownloadRecord
import com.xioneko.android.nekoanime.data.model.SearchRecord
import com.xioneko.android.nekoanime.data.model.ThemeConfig
import com.xioneko.android.nekoanime.data.model.WatchRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val SEARCH_HISTORY_MAX_SIZE = 10

@Singleton
class UserDataSource @Inject constructor(
    private val dataStore: DataStore<UserDataProto>
) {
    val userData = buildMap<String, Flow<*>> {
        put("searchHistory",
            dataStore.data.map { it.searchRecords })
        put("watchHistory",
            dataStore.data.map { it.watchRecords })
        put("downloadHistory",
            dataStore.data.map { it.downloadRecords })
        put("followedAnimeIds",
            dataStore.data.map { it.followedAnimeIds })
        put("interests",
            dataStore.data.map { it.interests })
        put("themeConfig",
            dataStore.data.map { it.themeConfig })
        put("updateAutoCheck",
            dataStore.data.map { it.updateAutoCheck })
    }

    suspend fun addSearchRecord(record: SearchRecord) {
        dataStore.updateData { userData ->
            val origin = userData.searchRecords.toMutableList()
            if (record in origin) userData
            else {
                if (origin.size == SEARCH_HISTORY_MAX_SIZE) origin.removeAt(0)
                userData.copy(
                    searchRecords = origin.apply { add(record) }
                )
            }
        }
    }

    suspend fun clearSearchRecord() {
        dataStore.updateData { userData ->
            userData.copy(searchRecords = emptyList())
        }
    }

    suspend fun addWatchRecord(animeId: Int, record: WatchRecord) {
        dataStore.updateData { userData ->
            val origin = userData.watchRecords.toMutableMap()
            userData.copy(
                watchRecords = origin.apply { put(animeId, record) }
            )
        }
    }

    suspend fun clearWatchRecords() {
        dataStore.updateData { userData ->
            userData.copy(watchRecords = emptyMap())
        }
    }

    suspend fun addDownloadRecord(animeId: Int, record: DownloadRecord) {
        dataStore.updateData { userData ->
            val origin = userData.downloadRecords.toMutableMap()
            userData.copy(
                downloadRecords = origin.apply { put(animeId, record) }
            )
        }
    }


    suspend fun setInterests(interests: List<String>) {
        dataStore.updateData { userData ->
            userData.copy(interests = interests)
        }
    }

    suspend fun addFollowedAnimeId(animeId: Int) {
        dataStore.updateData { userData ->
            val origin = userData.followedAnimeIds.toMutableList()
            userData.copy(
                followedAnimeIds = origin.apply { add(animeId) }
            )
        }
    }

    suspend fun unfollowedAnime(animeId: Int) {
        dataStore.updateData { userData ->
            val origin = userData.followedAnimeIds.toMutableList()
            userData.copy(
                followedAnimeIds = origin.apply { remove(animeId) }
            )
        }
    }


    suspend fun setThemeConfig(config: ThemeConfig) {
        dataStore.updateData { userData ->
            userData.copy(themeConfig = config)
        }
    }

    suspend fun setUpdateAutoCheck(enable: Boolean) {
        dataStore.updateData { userData ->
            userData.copy(
                updateAutoCheck = enable
            )
        }
    }

}