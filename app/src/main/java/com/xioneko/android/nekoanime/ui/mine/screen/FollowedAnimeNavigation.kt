package com.xioneko.android.nekoanime.ui.mine.screen

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object FollowedAnimeNavRoute

fun NavGraphBuilder.followedAnimeScreen(
    onAnimeClick: (Int, Int?, String?) -> Unit,
    onBackClick: () -> Unit
) {
    composable<FollowedAnimeNavRoute> {
        FollowedAnimeScreen(
            onAnimeClick = onAnimeClick,
            onBackClick = onBackClick
        )
    }
}

fun NavHostController.navigateToFollowedAnime() {
    navigate(FollowedAnimeNavRoute)
}