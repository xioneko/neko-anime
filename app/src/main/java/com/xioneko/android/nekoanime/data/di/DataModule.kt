package com.xioneko.android.nekoanime.data.di


import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.xioneko.android.nekoanime.data.AnimeDataValidator
import com.xioneko.android.nekoanime.data.SimpleMediaCache
import com.xioneko.android.nekoanime.data.model.Anime
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
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

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun simpleMediaCache(
        @ApplicationContext context: Context
    ) = object : SimpleMediaCache {
        override var instance = build()

        override fun clear() {
            instance.keys.forEach { instance.removeResource(it) }
            instance.release()
            instance = build()
        }

        private fun build(): SimpleCache {
            val cacheDir = File(context.cacheDir, "media")
            val cacheEvictor = LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024)
            val databaseProvider = StandaloneDatabaseProvider(context)
            return SimpleCache(cacheDir, cacheEvictor, databaseProvider)
        }
    }

}