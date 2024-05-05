package com.xioneko.android.nekoanime.ui.component


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.ui.util.isTablet
import kotlin.math.floor

@Composable
fun LazyAnimeGrid(
    modifier: Modifier = Modifier,
    verticalPadding: Dp = 0.dp,
    horizontalPadding: Dp = 0.dp,
    useExpandCardStyle: Boolean = false,
    horizontalSpacing: Dp = 12.dp,
    verticalSpacing: Dp = 5.dp,
    animeList: List<AnimeShell?>,
    onAnimeClick: (Int) -> Unit,
) {
    val isTablet = isTablet()
    val minCardWidth: Dp = remember(isTablet) {
        if (isTablet) 144.dp else 108.dp
    }
    val configuration = LocalConfiguration.current
    val colCnt = remember(configuration.screenWidthDp, minCardWidth) {
        floor(
            (configuration.screenWidthDp.dp.value - horizontalPadding.value)
                    / (minCardWidth.value + horizontalSpacing.value)
        )
            .toInt()
            .coerceIn(3, 6)
            .let { if (it == 5) 4 else it }
    }

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(colCnt),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        contentPadding = PaddingValues(horizontalPadding, verticalPadding),
    ) {
        animeList.forEachIndexed { index, anime ->
            item(index) {
                if (anime == null) {
                    PlaceholderAnimeCard()
                } else if (useExpandCardStyle) {
                    ExpandedAnimeCard(anime = anime, onClick = onAnimeClick)
                } else {
                    NarrowAnimeCard(anime = anime, onClick = onAnimeClick)
                }

            }

        }
    }
}