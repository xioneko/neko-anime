package com.xioneko.android.nekoanime.data.di


import com.xioneko.android.nekoanime.data.AnimeDataValidator
import com.xioneko.android.nekoanime.data.model.Anime
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Calendar
import javax.inject.Singleton

private const val DEFAULT_EXPIRATION: Long = 300_000 // 5分钟

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun animeDataValidator(): AnimeDataValidator =
        object : AnimeDataValidator {
            private val expiration: Long = DEFAULT_EXPIRATION

            override suspend fun isValid(item: Anime): Boolean =
                if (item.status == "已完结") true
                else Calendar.getInstance().timeInMillis - item.lastModified.timeInMillis < expiration
        }


}