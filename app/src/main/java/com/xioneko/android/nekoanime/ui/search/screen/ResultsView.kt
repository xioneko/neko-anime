package com.xioneko.android.nekoanime.ui.search.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xioneko.android.nekoanime.data.ANIME_LIST_PAGE_SIZE
import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.ui.component.LoadingDots
import com.xioneko.android.nekoanime.ui.search.SearchResult
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@Composable
internal fun ResultsView(
    input: String,
    fetch: (String, Int) -> Flow<Pair<Anime, Flow<Boolean>>>,
    onFollow: (Anime) -> Unit,
    onUnfollow: (Anime) -> Unit,
    onAnimeClick: (Int) -> Unit,
) {

    var hasMore by remember(input) { mutableStateOf(true) }
    val pageIndex = remember(input) { MutableStateFlow(0) }
    val results = remember(input) { mutableStateListOf<Pair<Anime, Flow<Boolean>>>() }

    val lazyListState = rememberLazyListState()
    val shouldFetchMore by remember(hasMore) {
        derivedStateOf {
            with(lazyListState.layoutInfo) {
                hasMore && totalItemsCount > 8 &&
                        visibleItemsInfo.lastOrNull()?.let {
                            it.index > totalItemsCount - ANIME_LIST_PAGE_SIZE / 4
                        } ?: false
            }
        }
    }
    var loadingPageCount by remember(input) { mutableStateOf(0) }

    if (shouldFetchMore) pageIndex.update { it + 2 }

    LaunchedEffect(input) {
        pageIndex.collect { index ->
            repeat(2) {
                launch {
                    fetch(input, index + it)
                        .onStart { loadingPageCount++ }
                        .onEach { results.add(it) }
                        .onCompletion { loadingPageCount-- }
                        .onEmpty { hasMore = false }
                        .collect()
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .fillMaxSize()
            .background(basicWhite),
        state = lazyListState,
        contentPadding = PaddingValues(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        reverseLayout = false,
        userScrollEnabled = true,
    ) {
        // TODO: 无搜索结果时给予反馈
        for (searchItem in results) {
            item(searchItem.first.id) {
                val followed by searchItem.second.collectAsStateWithLifecycle(true)
                SearchResult(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    anime = searchItem.first,
                    isFollowed = followed,
                    onClick = onAnimeClick,
                    onFollowAnime = onFollow,
                    onUnfollowAnime = onUnfollow
                )
            }
        }
        if (loadingPageCount > 0) {
            item(
                key = "Loading",
                contentType = "Loading",
                content = { LoadingDots() }
            )
        }
    }
}