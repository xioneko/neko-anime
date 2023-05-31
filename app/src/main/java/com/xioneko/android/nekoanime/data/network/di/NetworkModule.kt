package com.xioneko.android.nekoanime.data.network.di


import com.xioneko.android.nekoanime.data.network.VideoSourceManager
import com.xioneko.android.nekoanime.data.network.Yhdmz2VideoSource
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
        yhdmz2VideoSource: Yhdmz2VideoSource,
        yinghuacdVideoSource: YinghuacdVideoSource,
    ): VideoSourceManager = object : VideoSourceManager {
        override val sources = listOf(
            yinghuacdVideoSource,
            yhdmz2VideoSource
        )
        override var index: Int = 0
    }
}