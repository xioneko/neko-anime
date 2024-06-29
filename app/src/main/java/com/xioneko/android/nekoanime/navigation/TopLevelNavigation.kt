package com.xioneko.android.nekoanime.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import com.xioneko.android.nekoanime.ui.component.NekoAnimeBottomAppBar
import com.xioneko.android.nekoanime.ui.home.HomeNavRoute
import com.xioneko.android.nekoanime.ui.home.homeScreen
import com.xioneko.android.nekoanime.ui.mine.mineScreen
import com.xioneko.android.nekoanime.ui.schedule.scheduleScreen

const val TopLevelNavRoute = "top_level_route"

fun NavGraphBuilder.topLevelScreen(
    navController: NavHostController,
    navigateToCategory: (type: Int) -> Unit,
    navigateToFollowedAnime: () -> Unit,
    navigateToHistory: () -> Unit,
    navigateToDownload: () -> Unit,
    navigateToAnimePlay: (Int) -> Unit,
) {
    composable(TopLevelNavRoute) {

        val currentDestination =
            navController.currentBackStackEntryAsState().value?.destination

        Scaffold(
            bottomBar = {
                NekoAnimeBottomAppBar(
                    currentDestination = currentDestination,
                    onNavigateTo = {
                        navController.navigate(
                            route = it.route,
                            navOptions { launchSingleTop = true })
                    }
                )
            },
            contentWindowInsets = WindowInsets.navigationBars
        ) { padding ->
            NavHost(
                modifier = Modifier.zIndex(1f),
                navController = navController,
                startDestination = HomeNavRoute,
                contentAlignment = Alignment.Center,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {

                homeScreen(
                    padding = padding,
                    onAnimeClick = navigateToAnimePlay,
                    onCategoryClick = { navigateToCategory(1) },
                    onFollowedAnimeClick = navigateToFollowedAnime,
                    onHistoryClick = navigateToHistory,
                    navigateToCategory = navigateToCategory
                )

                scheduleScreen(
                    padding = padding,
                    onAnimeClick = navigateToAnimePlay,
                )

                mineScreen(
                    padding = padding,
                    onAnimeClick = navigateToAnimePlay,
                    onFollowedAnimeClick = navigateToFollowedAnime,
                    onHistoryClick = navigateToHistory,
                    onDownloadClick = navigateToDownload
                )
            }
        }
    }
}
