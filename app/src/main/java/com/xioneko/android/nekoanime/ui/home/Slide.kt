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
        animeId = 22287,
        title = "无职转生II",
        description = "艾莉丝消失后过了几个月。鲁迪乌斯尽管内心感到空虚，还是为了寻找母亲塞妮丝，来到巴榭兰特公国的第二都市罗森堡..."
    ),

    Slide2(
        imageId = R.drawable.slide2,
        animeId = 18248,
        title = "咒术回战 第二季",
        description = "虎杖他们击败道成肉身的「咒胎九相图」次男和三男，回收了宿傩的手指。而在背后暗中安排的五条，究竟有何用心...！？"
    ),

    Slide3(
        imageId = R.drawable.slide3,
        animeId = 18127,
        title = "BanG Dream! It's MyGO!!",
        description = "高一即将结束的春天。在羽丘女子学园里，每个人都在组乐团，晚了入学的爱音也为了快速融入班级，急着寻找乐团成员..."
    ),

    Slide4(
        imageId = R.drawable.slide4,
        animeId = 18326,
        title = "谎言游戏",
        description = "在以决定阶级的学园岛上，我——篠原绯吕斗参加了国内最困难的学园岛入学测验，以学园岛史上最快的速度夺得了'7星'。──没错，这一切当然全是一场骗局..."
    ),

    Slide5(
        imageId = R.drawable.slide5,
        animeId = 18464,
        title = "间谍教室 第二季",
        description = "达成刺客「尸」的任务之后，四名少女失踪了，克劳斯带着百合前去追查她们的下落。时间回溯到四天前..."
    )
}