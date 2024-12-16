package com.xioneko.android.nekoanime.data.network.repository

import com.xioneko.android.nekoanime.data.network.danmu.api.DanmuSession

interface DanmuRepository {
    suspend fun fetchDanmuSession(animeName: String, episodeName: String?): DanmuSession?
}