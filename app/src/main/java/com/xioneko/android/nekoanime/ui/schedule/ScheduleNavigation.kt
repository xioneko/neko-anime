package com.xioneko.android.nekoanime.ui.schedule

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object ScheduleNavRoute

fun NavGraphBuilder.scheduleScreen(
    padding: PaddingValues,
    onAnimeClick: (Int) -> Unit
) {
    composable<ScheduleNavRoute> {
        ScheduleScreen(
            padding = padding,
            onAnimeClick = onAnimeClick
        )
    }
}