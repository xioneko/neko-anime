package com.xioneko.android.nekoanime.data

import com.xioneko.android.nekoanime.data.model.Anime
import org.mobilenativefoundation.store.store5.Validator
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnimeDataValidator @Inject constructor() : Validator<Anime> {
    override suspend fun isValid(item: Anime): Boolean =
        Calendar.getInstance().timeInMillis - item.lastUpdate.timeInMillis <
                if (item.status.contains("更新")) 5 * 60 * 1000
                else 12 * 60 * 60 * 1000
}