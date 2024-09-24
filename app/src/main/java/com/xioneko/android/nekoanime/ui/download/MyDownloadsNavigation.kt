package com.xioneko.android.nekoanime.ui.download

import android.content.Context
import androidx.core.net.toUri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.xioneko.android.nekoanime.R
import com.xioneko.android.nekoanime.data.model.AnimeShell
import kotlinx.serialization.Serializable

@Serializable
data object MyDownloadsNavRoute

fun NavGraphBuilder.myDownloadsScreen(
    context: Context,
    onDownloadedAnimeClick: (AnimeShell) -> Unit,
    onBackClick: () -> Unit
) {

    composable<MyDownloadsNavRoute>(
        deepLinks = listOf(
            navDeepLink<MyDownloadsNavRoute>(
                basePath = "${context.getString(R.string.app_scheme)}://MyDownloads",
            )
        )
    ) {
        MyDownloadsScreen(
            onDownloadedAnimeClick = onDownloadedAnimeClick,
            onBackClick = onBackClick,
        )
    }
}

fun NavHostController.navigateToMyDownloads() {
    navigate(MyDownloadsNavRoute)
}

fun deepLinkToMyDownloads(context: Context) =
    "${context.getString(R.string.app_scheme)}://MyDownloads".toUri()
