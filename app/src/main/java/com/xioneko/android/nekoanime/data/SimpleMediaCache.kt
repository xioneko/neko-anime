package com.xioneko.android.nekoanime.data

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache

interface SimpleMediaCache {
    @get:UnstableApi
    var instance: SimpleCache

    @OptIn(UnstableApi::class)
    fun clear()
}