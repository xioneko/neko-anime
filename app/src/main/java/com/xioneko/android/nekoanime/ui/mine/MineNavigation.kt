package com.xioneko.android.nekoanime.ui.mine

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object MineNavRoute


fun NavGraphBuilder.mineScreen(
    padding: PaddingValues,
    onDownloadClick: () -> Unit,
    onFollowedAnimeClick: () -> Unit,
    onHistoryClick: () -> Unit,
) {
    composable<MineNavRoute> {
        MineScreen(
            padding = padding,
            onDownloadClick = onDownloadClick,
            onFollowedAnimeClick = onFollowedAnimeClick,
            onHistoryClick = onHistoryClick,
        )
    }
}

