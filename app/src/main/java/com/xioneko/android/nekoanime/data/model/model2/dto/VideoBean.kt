package com.xioneko.android.nekoanime.data.model.model2.dto

data class VideoBean(
    val title: String,
    val url: String,
    val episodeName: String,
    val episode: List<EpisodeBean>,
    val headers: Map<String, String> = emptyMap()
) {

}