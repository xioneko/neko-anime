package com.xioneko.android.nekoanime.data.network.di


import com.xioneko.android.nekoanime.data.network.VideoSourceManager
import com.xioneko.android.nekoanime.data.network.YhmgoVideoSource
import com.xioneko.android.nekoanime.data.network.YinghuacdVideoSource
import com.xioneko.android.nekoanime.data.network.util.ErrorHandlerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun okHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(ErrorHandlerInterceptor)
            .build()

    @Provides
    @Singleton
    fun videoDataSource(
        yhmgoVideoSource: YhmgoVideoSource,
        yinghuacdVideoSource: YinghuacdVideoSource,
    ): VideoSourceManager = object : VideoSourceManager {
        override val sources = listOf(
            yinghuacdVideoSource,
            yhmgoVideoSource
        )
        override var index: Int = 0
    }
}