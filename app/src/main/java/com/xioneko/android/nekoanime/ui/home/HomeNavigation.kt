package com.xioneko.android.nekoanime.ui.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val HomeNavRoute = "home_route"


fun NavGraphBuilder.homeScreen(
    padding: PaddingValues,
    onCategoryClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onAnimeClick: (Int) -> Unit,
    onFollowedAnimeClick: () -> Unit,
    navigateToCategory: (type: Int) -> Unit
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
