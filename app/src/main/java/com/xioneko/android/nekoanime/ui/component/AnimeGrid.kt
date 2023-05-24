package com.xioneko.android.nekoanime.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import com.xioneko.android.nekoanime.data.model.AnimeShell

@Composable
fun AnimeGrid(
    modifier: Modifier = Modifier,
    useExpandCardStyle: Boolean = false,
    animeList: List<AnimeShell?>,
    onAnimeClick: (Int) -> Unit,
) {
    assert(animeList.size % 3 == 0)

    Column(
        modifier = modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        repeat(animeList.size / 3) { row ->
            Layout(content = {
                for (i in row * 3 until (row + 1) * 3) {
                    if (animeList[i] == null) {
                        PlaceholderAnimeCard()
                    } else if (useExpandCardStyle) {
                        ExpandAnimeCard(anime = animeList[i]!!, onClick = onAnimeClick)
                    } else {
                        NarrowAnimeCard(anime = animeList[i]!!, onClick = onAnimeClick)
                    }
                }
            }, measurePolicy = { measurables, constraints ->
                val cardWidth = (constraints.maxWidth - 24.dp.roundToPx()) / 3
                val placeables =
                    measurables.map { it.measure(constraints.copy(maxWidth = cardWidth)) }

                layout(cardWidth, placeables.first().height) {
                    var x = 0
                    repeat(3) {
                        placeables[it].placeRelative(x, 0)
                        x += 12.dp.roundToPx() + cardWidth
                    }
                }
            })
        }
    }
}