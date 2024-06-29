package com.xioneko.android.nekoanime.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class SuggestsResponse(
    val code: Int,
    val msg: String,
    val page: Int,
    val pagecount: Int,
    val limit: Int,
    val total: Int,
    val list: List<Suggest>,
    val url: String
)

@Serializable
data class Suggest(
    val id: Int,
    val name: String,
    val en: String,
    val pic: String
)

