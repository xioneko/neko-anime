package com.xioneko.android.nekoanime.data.network.danmu.dto

import kotlinx.serialization.Serializable

@Serializable
data class DanmuInfo(
    val cid: Long,
    val p: String,
    val m: String
)

fun DanmuInfo.toDanmuku(): Danmuku? {
    val (time, mode, color, userId) = p.split(",").let {
        if (it.size < 4) return null else it
    }
    val timeSecs = time.toDoubleOrNull() ?: return null
    return Danmuku(
        id = cid.toString(),
        providerId = "",
        playTimeMillis = (timeSecs * 1000).toLong(),
        senderId = userId,
        location = when (mode.toIntOrNull()) {
            1 -> DanmukuLocation.NORMAL
            4 -> DanmukuLocation.BOTTOM
            5 -> DanmukuLocation.TOP
            else -> return null
        },
        text = m,
        color = color.toIntOrNull() ?: return null
    )
}

@Serializable
class DanmuInfoListResponse(
    val count: Int,
    val comments: List<DanmuInfo>
)

