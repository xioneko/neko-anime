package com.xioneko.android.nekoanime.ui.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

const val AnimePlayNavRoute = "anime_play_route"


@SuppressLint("SourceLockedOrientationActivity")
fun NavGraphBuilder.animePlayScreen(
    onTagClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    composable(
        route = "$AnimePlayNavRoute/{animeId}",
        arguments = listOf(navArgument("animeId") { type = NavType.IntType }),
    ) { backStackEntry ->

        val activity = LocalContext.current as? Activity

//        Log.d("Player", "Before Orientation: " + activity?.requestedOrientation.toString())

        // 由于播放页面可通过播放器全屏按钮锁定设备方向，因此需要在此处保存和恢复设备方向
        val previousOrientation = rememberSaveable {
            activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        LaunchedEffect(Unit) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        AnimePlayScreen(
            animeId = backStackEntry.arguments?.getInt("animeId")!!,
            onTagClick = {
                onTagClick(it)
                activity?.requestedOrientation = previousOrientation
            },
            onBackClick = {
                onBackClick()
                activity?.requestedOrientation = previousOrientation
            }
        )
    }
}

fun NavHostController.navigateToAnimePlay(animeId: Int) {
    navigate("$AnimePlayNavRoute/$animeId")
}