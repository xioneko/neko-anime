package com.xioneko.android.nekoanime.ui.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.net.toUri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.xioneko.android.nekoanime.R
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.ui.util.resetScreenBrightness
import kotlinx.serialization.Serializable

@Serializable
data class AnimePlayNavRoute(
    val animeId: Int,
    val episode: Int? = null,
    val episodeName: String? = ""
)


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("SourceLockedOrientationActivity")
fun NavGraphBuilder.animePlayScreen(
    context: Context,
    onTagClick: (String) -> Unit,
    onDownloadedAnimeClick: (AnimeShell) -> Unit,
    onBackClick: () -> Unit
) {
    composable<AnimePlayNavRoute>(
        deepLinks = listOf(
            navDeepLink<AnimePlayNavRoute>(
                basePath = "${context.getString(R.string.app_scheme)}://AnimePlay",
            )
        )
    ) { backStackEntry ->

        val activity = context as? Activity

//        Log.d("Orientation", "Before Orientation: " + activity?.requestedOrientation.toString())

        // 由于播放页面可通过播放器全屏按钮锁定设备方向，因此需要在此处保存和恢复设备方向
        val previousOrientation = rememberSaveable {
            activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        val disposeEffect = {
            activity?.requestedOrientation = previousOrientation
            context.resetScreenBrightness()
        }

        LaunchedEffect(Unit) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }


        val navRoute = backStackEntry.toRoute<AnimePlayNavRoute>()
        AnimePlayScreen(
            animeId = navRoute.animeId,
            episode = navRoute.episode,
            episodeName = navRoute.episodeName,
            onTagClick = {
                onTagClick(it)
                disposeEffect()
            },
            onDownloadedAnimeClick = {
                onDownloadedAnimeClick(it)
                disposeEffect()
            },
            onBackClick = {
                onBackClick()
                disposeEffect()
            }
        )
    }
}

fun NavHostController.navigateToAnimePlay(
    animeId: Int,
    episode: Int? = null,
    episodeName: String? = ""
) {
    navigate(AnimePlayNavRoute(animeId, episode, episodeName))
}

fun deepLinkToAnimePlay(context: Context, animeId: Int, episode: Int? = null): Uri {
    return "${context.getString(R.string.app_scheme)}://AnimePlay/$animeId?episode=$episode".toUri()
}