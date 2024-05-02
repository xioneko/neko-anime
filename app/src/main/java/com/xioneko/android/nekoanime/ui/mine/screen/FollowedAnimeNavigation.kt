package com.xioneko.android.nekoanime.ui.mine.screen

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val FollowedAnimeNavRoute = "followed_anime_route"

fun NavGraphBuilder.followedAnimeScreen(
    onAnimeClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    composable(
        route = FollowedAnimeNavRoute,
        arguments = emptyList(),
        deepLinks = emptyList(),
        enterTransition = null,
        exitTransition = null,
        popEnterTransition = null,
        popExitTransition = null,
    ) {
        FollowedAnimeScreen(
            onAnimeClick = onAnimeClick,
            onBackClick = onBackClick
        )
    }
}