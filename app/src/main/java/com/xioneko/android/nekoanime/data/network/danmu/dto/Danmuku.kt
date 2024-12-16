package com.xioneko.android.nekoanime.data.network.danmu.dto

import androidx.compose.runtime.Immutable

@Immutable
class DanmakuPresentation(
    val danmaku: Danmuku,
    val isSelf: Boolean,
) {
    val id get() = danmaku.id
}

@Immutable
data class Danmuku(
    val id: String,
    val providerId: String,
    val playTimeMillis: Long,
    val senderId: String,
    val location: DanmukuLocation,
    val text: String,
    val color: Int, // RGB
)


enum class DanmukuLocation {
    TOP,
    BOTTOM,
    NORMAL,
}