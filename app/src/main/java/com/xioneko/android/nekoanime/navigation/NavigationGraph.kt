package com.xioneko.android.nekoanime.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.xioneko.android.nekoanime.ui.category.categoryScreen
import com.xioneko.android.nekoanime.ui.category.navigateToCategory
import com.xioneko.android.nekoanime.ui.download.downloadedAnimeScreen
import com.xioneko.android.nekoanime.ui.download.myDownloadsScreen
import com.xioneko.android.nekoanime.ui.download.navigateToDownloadedAnime
import com.xioneko.android.nekoanime.ui.download.navigateToMyDownloads
import com.xioneko.android.nekoanime.ui.mine.screen.followedAnimeScreen
import com.xioneko.android.nekoanime.ui.mine.screen.historyScreen
import com.xioneko.android.nekoanime.ui.mine.screen.navigateToFollowedAnime
import com.xioneko.android.nekoanime.ui.mine.screen.navigateToHistory
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
    val context = LocalContext.current

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
            navigateToCategory = navController::navigateToCategory,
            navigateToFollowedAnime = navController::navigateToFollowedAnime,
            navigateToHistory = navController::navigateToHistory,
            navigateToDownload = navController::navigateToMyDownloads,
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

        myDownloadsScreen(
            context = context,
            onDownloadedAnimeClick = navController::navigateToDownloadedAnime,
            onBackClick = navController::popBackStack,
        )

        downloadedAnimeScreen(
            context = context,
            onAnimeClick = navController::navigateToAnimePlay,
            onBackClick = navController::popBackStack,
        )

        animePlayScreen(
            context = context,
            onTagClick = {
                // TODO: 考虑跳转到搜索页
//                navController.navigateToCategory(genre = it)
            },
            onDownloadedAnimeClick = navController::navigateToDownloadedAnime,
            onBackClick = { navController.popBackStack() },
        )

    }
}