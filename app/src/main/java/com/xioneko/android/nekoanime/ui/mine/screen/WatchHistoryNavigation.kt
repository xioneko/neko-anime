package com.xioneko.android.nekoanime.ui.mine.screen

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable


const val WatchHistoryNavRoute = "watch_history_route"

fun NavGraphBuilder.historyScreen(
    onAnimeClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    composable(
        route = WatchHistoryNavRoute,
        arguments = emptyList(),
        deepLinks = emptyList(),
        enterTransition = null,
        exitTransition = null,
        popEnterTransition = null,
        popExitTransition = null,
    ) {
        WatchHistoryScreen(
            onAnimeClick = onAnimeClick,
            onBackClick = onBackClick
        )
    }
}