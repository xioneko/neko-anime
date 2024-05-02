package com.xioneko.android.nekoanime.ui.mine

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val MineNavRoute = "mine_route"


fun NavGraphBuilder.mineScreen(
    padding: PaddingValues,
    onDownloadClick: () -> Unit,
    onFollowedAnimeClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onAnimeClick: (Int) -> Unit,
) {
    composable(
        route = MineNavRoute,
        arguments = emptyList(),
        deepLinks = emptyList(),
        enterTransition = null,
        exitTransition = null,
        popEnterTransition = null,
        popExitTransition = null,
    ) {
        MineScreen(
            padding = padding,
            onDownloadClick = onDownloadClick,
            onFollowedAnimeClick = onFollowedAnimeClick,
            onHistoryClick = onHistoryClick,
            onAnimeClick = onAnimeClick
        )
    }
}

