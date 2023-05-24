package com.xioneko.android.nekoanime.data.network

interface VideoSourceManager {
    val sources: List<VideoDataSource>
    var index: Int
    val currentSource: VideoDataSource
        get() = sources[index]

    fun tryNext() = hasNext().also { if (it) index++ }

    fun hasNext() = index < sources.size - 1
}