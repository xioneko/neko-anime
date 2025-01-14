package com.xioneko.android.nekoanime.data.network.danmu

import com.xioneko.android.nekoanime.data.network.danmu.api.DanmuSession
import com.xioneko.android.nekoanime.data.network.repository.DanmuRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DanmuRepositoryImpl @Inject constructor(
    private val danmuProvider: DanmuProvider
) : DanmuRepository {
    override suspend fun fetchDanmuSession(
        animeName: String,
        episodeName: String?
    ): DanmuSession? {
        return try {
            danmuProvider.fetch(animeName, episodeName)
        } catch (e: Exception) {
            null
        }
    }
}