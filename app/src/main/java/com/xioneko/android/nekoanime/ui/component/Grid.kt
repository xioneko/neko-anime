package com.xioneko.android.nekoanime.ui.component


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.ui.util.isTablet
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun AnimeGrid(
    modifier: Modifier = Modifier,
    useExpandCardStyle: Boolean = false,
    horizontalSpacing: Dp = 12.dp,
    verticalSpacing: Dp = 5.dp,
    animeList: List<AnimeShell?>,
    onAnimeClick: (Int) -> Unit,
) {
    val isTablet = isTablet()

    val minCardWidth: Dp = if (isTablet) 144.dp else 96.dp

    Layout(
        modifier = modifier,
        content = {
            animeList.forEach {
                if (it == null) {
                    PlaceholderAnimeCard()
                } else if (useExpandCardStyle) {
                    ExpandedAnimeCard(anime = it, onClick = onAnimeClick)
                } else {
                    NarrowAnimeCard(anime = it, onClick = onAnimeClick)
                }
            }
        }, measurePolicy = { measurables, constraints ->
            var colCnt =
                floor((constraints.maxWidth) / (minCardWidth.toPx() + horizontalSpacing.toPx())).toInt()
                    .coerceIn(3, 6)
            if (colCnt == 5) colCnt = 4

            val width =
                (constraints.maxWidth - (colCnt - 1) * horizontalSpacing.roundToPx()) / colCnt

            val placeables = measurables.map { it.measure(constraints.copy(maxWidth = width)) }
            val height = placeables.first().height
            val rowCnt = ceil(placeables.size / colCnt.toFloat()).toInt()

            layout(
                constraints.maxWidth,
                rowCnt * height + (rowCnt - 1) * verticalSpacing.roundToPx()
            ) {
                placeables.forEachIndexed { index, placeable ->
                    val row = index / colCnt
                    val col = index % colCnt
                    placeable.placeRelative(
                        col * (width + horizontalSpacing.roundToPx()),
                        row * (height + verticalSpacing.roundToPx())
                    )
                }
            }
        })
}