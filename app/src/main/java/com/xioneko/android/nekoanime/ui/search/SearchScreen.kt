package com.xioneko.android.nekoanime.ui.search

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.zIndex
import com.xioneko.android.nekoanime.ui.search.screen.CandidatesView
import com.xioneko.android.nekoanime.ui.search.screen.ResultsView
import com.xioneko.android.nekoanime.ui.search.screen.SearchHistoryView

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel,
    uiState: SearchBarState,
    onEnterExit: (Boolean) -> Unit,
    onCategoryClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onAnimeClick: (Int) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    var searchText by remember { mutableStateOf("") }
    var shouldShowResults by remember { mutableStateOf(false) }
    val shouldShowCandidates = searchText.isNotBlank()
    val shouldShowHistory = uiState.searching

    val onSearch = {
        val keyword = searchText.trim()
        if (keyword.isNotEmpty()) {
            focusManager.clearFocus()
            shouldShowResults = true
            viewModel.addSearchRecord(keyword)
        }
    }
    val onExit = {
        onEnterExit(false)
        shouldShowResults = false
        focusManager.clearFocus()
        searchText = ""
    }


    if (uiState.searching) {

        DisposableEffect(backDispatcher) {
            val backCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (shouldShowResults) {
                        shouldShowResults = false
                        searchText = ""
                        uiState.focusRequester.requestFocus()
                    } else onExit()
                }
            }
            backDispatcher?.addCallback(backCallback)
            onDispose { backCallback.remove() }
        }
    }

    Column(modifier) {
        NekoAnimeSearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f),
            text = searchText,
            searching = uiState.searching,
            focusRequester = uiState.focusRequester,
            searchBarState = uiState,
            onLeftIconClick = onHistoryClick,
            onRightIconClick = onCategoryClick,
            onInputChange = { searchText = it },
            onFocusChange = { focused ->
                if (focused) {
                    shouldShowResults = false
                    onEnterExit(true)
                } else {
                    onExit()
                }
            },
            onSearch = onSearch,
        )

        Box {
            androidx.compose.animation.AnimatedVisibility(
                visible = shouldShowHistory,
                enter = fadeIn(), exit = fadeOut()
            ) {
                SearchHistoryView(
                    source = viewModel.searchHistory,
                    onClearHistory = viewModel::clearSearchHistory,
                    onRecordClick = {
                        searchText = it
                        onSearch()
                    }
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = shouldShowCandidates,
                enter = fadeIn(), exit = fadeOut()
            ) {
                CandidatesView(
                    input = searchText.trim(),
                    fetch = viewModel::getCandidatesOf,
                    onCandidateClick = {
                        searchText = it
                        onSearch()
                    }
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = shouldShowResults,
                enter = fadeIn(), exit = fadeOut()
            ) {
                ResultsView(
                    input = searchText.trim(),
                    fetch = viewModel::searchAnime,
                    onFollow = viewModel::addFollowedAnime,
                    onUnfollow = viewModel::unfollowedAnime,
                    onAnimeClick = onAnimeClick,
                )
            }
        }
    }
}