package com.xioneko.android.nekoanime.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.xioneko.android.nekoanime.ui.category.categoryScreen
import com.xioneko.android.nekoanime.ui.category.navigateToCategory
import com.xioneko.android.nekoanime.ui.mine.screen.FollowedAnimeNavRoute
import com.xioneko.android.nekoanime.ui.mine.screen.WatchHistoryNavRoute
import com.xioneko.android.nekoanime.ui.mine.screen.followedAnimeScreen
import com.xioneko.android.nekoanime.ui.mine.screen.historyScreen
import com.xioneko.android.nekoanime.ui.player.animePlayScreen
import com.xioneko.android.nekoanime.ui.player.navigateToAnimePlay
import com.xioneko.android.nekoanime.ui.search.navigateToSearchScreen
import com.xioneko.android.nekoanime.ui.search.searchScreen


@Composable
fun NekoAnimeNavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val topLevelNavController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = TopLevelNavRoute,
        modifier = modifier,
        contentAlignment = Alignment.Center,
        enterTransition = { slideInHorizontally { it } },
        exitTransition = { slideOutHorizontally { -it / 4 } },
        popEnterTransition = { slideInHorizontally { -it / 4 } },
        popExitTransition = { slideOutHorizontally { it } }
    ) {
        topLevelScreen(
            navController = topLevelNavController,
            navigateToCategory = { navController.navigateToCategory(genre = it) },
            navigateToFollowedAnime = { navController.navigate(FollowedAnimeNavRoute) },
            navigateToHistory = { navController.navigate(WatchHistoryNavRoute) },
            navigateToDownload = { },
            navigateToAnimePlay = navController::navigateToAnimePlay,
        )

        searchScreen(
            onAnimeClick = navController::navigateToAnimePlay,
            onBackClick = navController::popBackStack
        )


        categoryScreen(
            onAnimeClick = navController::navigateToAnimePlay,
            onSearchClick = navController::navigateToSearchScreen,
            onBackClick = navController::popBackStack,
        )

        followedAnimeScreen(
            onAnimeClick = navController::navigateToAnimePlay,
            onBackClick = navController::popBackStack,
        )

        historyScreen(
            onAnimeClick = navController::navigateToAnimePlay,
            onBackClick = navController::popBackStack,
        )

        animePlayScreen(
            onGenreClick = { navController.navigateToCategory(genre = it) },
            onBackClick = { navController.popBackStack() },
        )

    }
}