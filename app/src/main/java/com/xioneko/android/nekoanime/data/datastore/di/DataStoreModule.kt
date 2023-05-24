package com.xioneko.android.nekoanime.data.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.xioneko.android.nekoanime.data.datastore.model.UserDataProto
import com.xioneko.android.nekoanime.data.datastore.serializer.LocalStorageSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun dataStore(
        @ApplicationContext context: Context,
        localStorageSerializer: LocalStorageSerializer
    ): DataStore<UserDataProto> =
        DataStoreFactory.create(
            serializer = localStorageSerializer
        ) {
            context.dataStoreFile("user_data.pb")
        }
}