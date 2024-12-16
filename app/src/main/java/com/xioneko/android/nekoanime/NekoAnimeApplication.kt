package com.xioneko.android.nekoanime

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NekoAnimeApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        _instance = this
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .crossfade(true)
            .build()

    companion object {
        private lateinit var _instance: Application

        fun getInstance(): Context {
            return _instance
        }
    }
}