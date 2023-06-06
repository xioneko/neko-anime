package com.xioneko.android.nekoanime.navigation

import com.xioneko.android.nekoanime.ui.home.HomeNavRoute
import com.xioneko.android.nekoanime.ui.mine.MineNavRoute
import com.xioneko.android.nekoanime.ui.schedule.ScheduleNavRoute
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons


enum class TopLevelScreen(
    val route: String,
    val iconId: Int,
    val label: String,
) {
    HOME(
        HomeNavRoute,
        NekoAnimeIcons.Animated.home,
        "首页"
    ),
    SCHEDULE(
        ScheduleNavRoute,
        NekoAnimeIcons.Animated.calendar,
        "时间表"
    ),
    MINE(
        MineNavRoute,
        NekoAnimeIcons.Animated.mine,
        "我的"
    )
}