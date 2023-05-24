package com.xioneko.android.nekoanime.ui.mine.screen

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable

const val WatchHistoryNavRoute = "watch_history_route"

@OptIn(ExperimentalAnimationApi::class)
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