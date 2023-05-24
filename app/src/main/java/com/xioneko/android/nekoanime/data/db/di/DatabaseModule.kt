package com.xioneko.android.nekoanime.data.db.di

import android.content.Context
import androidx.room.Room
import com.xioneko.android.nekoanime.data.db.AnimeDatabase
import com.xioneko.android.nekoanime.data.db.dao.AnimeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun animeDatabase(
        @ApplicationContext context: Context
    ): AnimeDatabase = Room.databaseBuilder(
        context = context,
        klass = AnimeDatabase::class.java,
        name = "anime-database"
    )
        .build()

    @Provides
    @Singleton
    fun animeDao(
        animeDatabase: AnimeDatabase
    ): AnimeDao = animeDatabase.animeDao()
}