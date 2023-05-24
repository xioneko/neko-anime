package com.xioneko.android.nekoanime.ui.mine

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable

const val MineNavRoute = "mine_route"

@OptIn(ExperimentalAnimationApi::class)
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

