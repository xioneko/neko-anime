package com.xioneko.android.nekoanime.ui.download

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.xioneko.android.nekoanime.R
import com.xioneko.android.nekoanime.data.model.AnimeShell
import kotlinx.serialization.Serializable

@Serializable
data class DownloadedAnimeNavRoute(
    val animeId: Int,
    val name: String? = null,
    val imageUrl: String? = null,
)

fun NavGraphBuilder.downloadedAnimeScreen(
    context: Context,
    onAnimeClick: (Int, Int) -> Unit,
    onBackClick: () -> Unit
) {

    composable<DownloadedAnimeNavRoute>(
        deepLinks = listOf(
            navDeepLink<DownloadedAnimeNavRoute>(
                basePath = "${context.getString(R.string.app_scheme)}://DownloadedAnime"
            )
        )
    ) { backStackEntry ->
        val navRoute = backStackEntry.toRoute<DownloadedAnimeNavRoute>()

        DownloadedAnimeScreen(
            animeId = navRoute.animeId,
            animeName = navRoute.name,
            imageUrl = navRoute.imageUrl,
            onAnimeClick = onAnimeClick,
            onBackClick = onBackClick
        )
    }
}


fun NavHostController.navigateToDownloadedAnime(anime: AnimeShell) {
    navigate(
        DownloadedAnimeNavRoute(
            animeId = anime.id,
            name = anime.name,
            imageUrl = anime.imageUrl
        )
    )
}

fun deepLinkToDownloadedAnime(
    context: Context,
    animeId: Int,
    animeName: String? = null,
    imageUrl: String? = null,
): Uri {
    return "${
        context.getString(R.string.app_scheme)
    }://DownloadedAnime/$animeId?animeName=$animeName&imageUrl=$imageUrl".toUri()
}