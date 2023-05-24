package com.xioneko.android.nekoanime.ui.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable

const val HomeNavRoute = "home_route"

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.homeScreen(
    padding: PaddingValues,
    onCategoryClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onAnimeClick: (Int) -> Unit,
    onFollowedAnimeClick: () -> Unit,
    navigateToCategory:  (genre: String) -> Unit
) {
    composable(
        route = HomeNavRoute,
    ) {
        HomeScreen(
            padding = padding,
            onCategoryClick = onCategoryClick,
            onHistoryClick = onHistoryClick,
            onAnimeClick = onAnimeClick,
            onFollowedAnimeClick = onFollowedAnimeClick,
            navigateToCategory = navigateToCategory
        )
    }
}
