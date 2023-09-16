package com.xioneko.android.nekoanime.ui.search.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xioneko.android.nekoanime.data.ANIME_LIST_PAGE_SIZE
import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.ui.component.LoadingDots
import com.xioneko.android.nekoanime.ui.search.ResultsViewState
import com.xioneko.android.nekoanime.ui.search.SearchResult
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import kotlinx.coroutines.flow.update


@Composable
internal fun ResultsView(
    uiState: ResultsViewState,
    onFollow: (Anime) -> Unit,
    onUnfollow: (Anime) -> Unit,
    onAnimeClick: (Int) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val shouldFetchMore by remember(uiState.hasMore.value) {
        derivedStateOf {
            with(lazyListState.layoutInfo) {
                if (!uiState.hasMore.value) false
                else (totalItemsCount > 8 &&
                        visibleItemsInfo.lastOrNull()?.let {
                            it.index > totalItemsCount - ANIME_LIST_PAGE_SIZE / 4
                        } ?: false)

            }
        }
    }
    if (shouldFetchMore) uiState.pageIndexFlow.update { it + 1 }

    LazyColumn(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .fillMaxSize()
            .background(basicWhite),
        state = lazyListState,
        contentPadding = PaddingValues(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        reverseLayout = false,
        userScrollEnabled = true,
    ) {
        // TODO: 无搜索结果时给予反馈
        items(uiState.results, { it.first.id }, { "Anime Result" }) { item ->
            val followed by item.second.collectAsStateWithLifecycle(true)
            SearchResult(
                modifier = Modifier.fillMaxWidth(),
                anime = item.first,
                isFollowed = followed,
                onClick = onAnimeClick,
                onFollowAnime = onFollow,
                onUnfollowAnime = onUnfollow
            )
        }
        if (uiState.loadingPageCount.value > 0) {
            item(
                key = "Loading",
                contentType = "Loading",
                content = { LoadingDots() }
            )
        }
    }
}