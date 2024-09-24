package com.xioneko.android.nekoanime.data.di


import com.xioneko.android.nekoanime.data.AnimeDataValidator
import com.xioneko.android.nekoanime.data.model.Anime
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Calendar
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun animeDataValidator(): AnimeDataValidator =
        object : AnimeDataValidator {

            override suspend fun isValid(item: Anime): Boolean =
                Calendar.getInstance().timeInMillis - item.lastUpdate.timeInMillis <
                        if (item.status.contains("更新")) 5 * 60 * 1000
                        else 12 * 60 * 60 * 1000
        }
}