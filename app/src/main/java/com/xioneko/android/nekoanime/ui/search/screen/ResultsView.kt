package com.xioneko.android.nekoanime.ui.search.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xioneko.android.nekoanime.data.ANIME_LIST_PAGE_SIZE
import com.xioneko.android.nekoanime.ui.component.LoadingDots
import com.xioneko.android.nekoanime.ui.component.NarrowAnimeCard
import com.xioneko.android.nekoanime.ui.component.NoResults
import com.xioneko.android.nekoanime.ui.search.ResultsViewState
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.util.getAspectRadio
import com.xioneko.android.nekoanime.ui.util.isTablet
import kotlinx.coroutines.flow.update


@Composable
fun ResultsView(
    uiState: ResultsViewState,
    onAnimeClick: (Int, Int?, String?) -> Unit,
) {
    val aspectRatio = getAspectRadio()
    val isTablet = isTablet()
    val lazyGridState = rememberLazyGridState()
    val shouldShowLoadingDots by remember { derivedStateOf { uiState.loadingPageCount.value > 0 } }
    val shouldFetchMore by remember(uiState.hasMore.value) {
        derivedStateOf {
            with(lazyGridState.layoutInfo) {
                uiState.hasMore.value &&
                        totalItemsCount > 12 &&
                        visibleItemsInfo.last().index > totalItemsCount - ANIME_LIST_PAGE_SIZE / 4

            }
        }
    }

    if (shouldFetchMore) uiState.pageIndexFlow.update { it + 1 }

    Box(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .fillMaxSize()
            .background(basicWhite),
    ) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            state = lazyGridState,
            columns = GridCells.Fixed(if (isTablet) 4 else if (aspectRatio < 0.56) 6 else 3),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            for (animeShell in uiState.results) {
                item(animeShell.id) {
                    NarrowAnimeCard(anime = animeShell, onClick = onAnimeClick)
                }
            }
            if (shouldShowLoadingDots) {
                item(
                    key = "Loading",
                    contentType = "Loading",
                    span = { GridItemSpan(maxLineSpan) },
                    content = { LoadingDots() }
                )
            }
        }

        if (!uiState.hasMore.value && uiState.results.isEmpty()) {
            NoResults(
                modifier = Modifier.fillMaxSize(),
                text = "没有找到相关结果，换个词试试吧~",
            )
        }
    }
}