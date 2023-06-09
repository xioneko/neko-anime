package com.xioneko.android.nekoanime.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.xioneko.android.nekoanime.ui.category.categoryScreen
import com.xioneko.android.nekoanime.ui.category.navigateToCategory
import com.xioneko.android.nekoanime.ui.mine.screen.FollowedAnimeNavRoute
import com.xioneko.android.nekoanime.ui.mine.screen.WatchHistoryNavRoute
import com.xioneko.android.nekoanime.ui.mine.screen.followedAnimeScreen
import com.xioneko.android.nekoanime.ui.mine.screen.historyScreen
import com.xioneko.android.nekoanime.ui.player.AnimePlayNavRoute
import com.xioneko.android.nekoanime.ui.player.animePlayScreen
import com.xioneko.android.nekoanime.ui.player.navigateToAnimePlay
import com.xioneko.android.nekoanime.ui.search.SearchNavRoute
import com.xioneko.android.nekoanime.ui.search.searchScreen


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NekoAnimeNavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val navOptions = navOptions { launchSingleTop = true }
    val context = LocalContext.current
    val topLevelNavController = rememberAnimatedNavController()

    AnimatedNavHost(
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
            navigateToDownload = {  },
            navigateToAnimePlay = navController::navigateToAnimePlay,
        )

        searchScreen(
            onAnimeClick = { navController.navigate(AnimePlayNavRoute) },
            onBackClick = navController::popBackStack
        )


        categoryScreen(
            onAnimeClick = navController::navigateToAnimePlay,
            onSearchClick = { navController.navigate(SearchNavRoute, navOptions) },
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
            onGenreClick = {
                context.resetOrientation()
                navController.navigateToCategory(genre = it)
            },
            onAnimeClick = {
                context.resetOrientation()
                navController.navigateToAnimePlay(it)
            },
            onBackClick = {
                context.resetOrientation()
                navController.popBackStack()
            },
        )

    }
}

@SuppressLint("SourceLockedOrientationActivity")
private fun Context.resetOrientation() {
    (this as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}