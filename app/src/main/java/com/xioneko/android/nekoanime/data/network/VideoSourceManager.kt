package com.xioneko.android.nekoanime.data.network

import android.util.Log

interface VideoSourceManager {
    val sources: List<VideoDataSource>
    var index: Int
    val currentSource: VideoDataSource
        get() = sources[index]

    fun tryNext() = hasNext().also {
        if (it) {
            index++
            Log.d("Video", "尝试下一个视频源")
        }
    }

    fun hasNext() = index < sources.size - 1
}