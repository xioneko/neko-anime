package com.xioneko.android.nekoanime.ui.home

import com.xioneko.android.nekoanime.R


enum class Slide(
    val imageId: Int,
    val animeId: Int,
    val title: String,
    val description: String,
) {
    Slide1(
        imageId = R.drawable.slide1,
        animeId = 22285,
        title = "鬼灭之刃 锻刀村篇",
        description = "霞柱恋柱迎战，炭治郎新刀亮相"
    ),

    Slide2(
        imageId = R.drawable.slide2,
        animeId = 23086,
        title = "放学后失眠的你",
        description = "“你的不眠之夜一定也有其意义。”"
    ),

    Slide3(
        imageId = R.drawable.slide3,
        animeId = 23167,
        title = "国王排名 勇气的宝箱",
        description = "讲述波吉、卡克及其同伴们那不为人知的“勇气的故事”"
    ),

    Slide4(
        imageId = R.drawable.slide4,
        animeId = 23140,
        title = "我家的英雄",
        description = "父亲为了女儿，走上了修罗之道，高潮迭起的悬疑故事就此开幕"
    ),

    Slide5(
        imageId = R.drawable.slide5,
        animeId = 23085,
        title = "为美好的世界献上爆焰！",
        description = "红魔族首屈一指的天才魔法师惠惠为了学习禁忌爆裂魔法，在修练与学校生活之间过着繁忙的每一天"
    )
}