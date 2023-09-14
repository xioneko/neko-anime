package com.xioneko.android.nekoanime.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.xioneko.android.nekoanime.ui.util.isTablet

@Composable
fun NekoAnimeTheme(
    content: @Composable () -> Unit
) {
    val isTablet = isTablet()

    MaterialTheme(
        typography = if (isTablet) NekoAnimeTypography.tablet else NekoAnimeTypography.phone,
        content = content
    )
}