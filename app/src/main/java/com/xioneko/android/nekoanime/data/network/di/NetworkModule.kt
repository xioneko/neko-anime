package com.xioneko.android.nekoanime.data.network.di


import com.xioneko.android.nekoanime.data.network.util.ErrorHandlerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
//    @Provides
//    @Singleton
//    fun okHttpClient(
//        @ApplicationContext context: Context,
//    ): OkHttpClient = OkHttpClient
//        .Builder()
//        .cache(Cache(File(context.cacheDir, "http_cache"), 10 * 1024 * 1024))
//        .addInterceptor(ErrorHandlerInterceptor)
//        .build()

    @Provides
    @Singleton
    fun createHttpClient(): OkHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(ErrorHandlerInterceptor)
        .build()
}