package com.xioneko.android.nekoanime.ui.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object HomeNavRoute


fun NavGraphBuilder.homeScreen(
    padding: PaddingValues,
    onCategoryClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onAnimeClick: (Int, Int?, String?) -> Unit,
    onFollowedAnimeClick: () -> Unit,
    navigateToCategory: (type: Int) -> Unit
) {
    composable<HomeNavRoute> {
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
