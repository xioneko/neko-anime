package com.xioneko.android.nekoanime.data.model

/**
 * 动漫展示页
 */
data class AnimeShell(
    val id: Int,
    val name: String,
    val imageUrl: String? = null,
    val status: String
)