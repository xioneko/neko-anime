package com.xioneko.android.nekoanime.data.network.di

import com.xioneko.android.nekoanime.data.network.danmu.DandanplayDanmuProvider
import com.xioneko.android.nekoanime.data.network.danmu.DanmuProvider
import com.xioneko.android.nekoanime.data.network.danmu.DanmuRepositoryImpl
import com.xioneko.android.nekoanime.data.network.repository.DanmuRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ApiModule {
    //弹幕工厂注入
    @Singleton
    @Binds
    abstract fun provideDanmakuRepository(danmakuRepositoryImpl: DanmuRepositoryImpl): DanmuRepository

    @Singleton
    @Binds
    abstract fun provideDandanplayProvider(dandanplayDanmakuProvider: DandanplayDanmuProvider): DanmuProvider
}