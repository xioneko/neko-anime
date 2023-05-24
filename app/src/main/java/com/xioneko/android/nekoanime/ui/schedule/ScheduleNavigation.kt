package com.xioneko.android.nekoanime.ui.schedule

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable

const val ScheduleNavRoute = "schedule_route"

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.scheduleScreen(
    padding: PaddingValues,
    onAnimeClick: (Int) -> Unit
) {
    composable(
        route = ScheduleNavRoute,
        arguments = emptyList(),
        deepLinks = emptyList(),
        enterTransition = null,
        exitTransition = null,
        popEnterTransition = null,
        popExitTransition = null,
    ) {
        ScheduleScreen(
            padding = padding,
            onAnimeClick = onAnimeClick
        )
    }
}