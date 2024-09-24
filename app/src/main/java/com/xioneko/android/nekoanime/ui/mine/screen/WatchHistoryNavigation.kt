package com.xioneko.android.nekoanime.ui.mine.screen

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object WatchHistoryNavRoute

fun NavGraphBuilder.historyScreen(
    onAnimeClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    composable<WatchHistoryNavRoute> {
        WatchHistoryScreen(
            onAnimeClick = onAnimeClick,
            onBackClick = onBackClick
        )
    }
}

fun NavHostController.navigateToHistory() {
    navigate(WatchHistoryNavRoute)
}