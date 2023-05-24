package com.xioneko.android.nekoanime.ui.player

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.composable

const val AnimePlayNavRoute = "anime_play_route"


@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.animePlayScreen(
    onGenreClick: (String) -> Unit,
    onAnimeClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    composable(
        route = "$AnimePlayNavRoute/{animeId}",
        arguments = listOf(navArgument("animeId") { type = NavType.IntType }),
    ) {backStackEntry ->
        AnimePlayScreen(
            animeId = backStackEntry.arguments?.getInt("animeId")!!,
            onAnimeClick = onAnimeClick,
            onGenreClick = onGenreClick,
            onBackClick = onBackClick
        )
    }
}

fun NavHostController.navigateToAnimePlay(animeId: Int) {
    navigate("$AnimePlayNavRoute/$animeId")
}