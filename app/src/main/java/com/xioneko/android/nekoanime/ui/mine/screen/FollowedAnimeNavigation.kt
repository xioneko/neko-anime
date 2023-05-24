package com.xioneko.android.nekoanime.ui.mine.screen

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable

const val FollowedAnimeNavRoute = "followed_anime_route"

@OptIn(ExperimentalAnimationApi::class)
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