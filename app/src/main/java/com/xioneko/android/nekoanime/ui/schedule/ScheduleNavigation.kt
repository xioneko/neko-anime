package com.xioneko.android.nekoanime.ui.schedule

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val ScheduleNavRoute = "schedule_route"

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