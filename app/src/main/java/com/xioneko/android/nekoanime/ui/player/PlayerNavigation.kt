package com.xioneko.android.nekoanime.ui.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.composable

const val AnimePlayNavRoute = "anime_play_route"


@SuppressLint("SourceLockedOrientationActivity")
@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.animePlayScreen(
    onGenreClick: (String) -> Unit,
    onAnimeClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    composable(
        route = "$AnimePlayNavRoute/{animeId}",
        arguments = listOf(navArgument("animeId") { type = NavType.IntType }),
    ) { backStackEntry ->

        val activity = LocalContext.current as? Activity
        val shouldLockOrientation = rememberSaveable {
            activity?.let {
                it.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } ?: false
        }
        val restoreOrientationSetting = remember {
            {
                if (shouldLockOrientation) {
//                    Log.d("ROTATE", "Lock")
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else {
//                    Log.d("ROTATE", "UnLock")
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
        }

        AnimePlayScreen(
            animeId = backStackEntry.arguments?.getInt("animeId")!!,
            onAnimeClick = onAnimeClick,
            onGenreClick = {
                onGenreClick(it)
                restoreOrientationSetting()
            },
            onBackClick = {
                onBackClick()
                restoreOrientationSetting()
            }
        )
    }
}

fun NavHostController.navigateToAnimePlay(animeId: Int) {
    navigate("$AnimePlayNavRoute/$animeId")
}