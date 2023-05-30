package com.xioneko.android.nekoanime.ui.search

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable

const val SearchNavRoute = "search_route"

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.searchScreen(
    onAnimeClick: (Int) -> Unit,
    onBackClick: () -> Unit,
) {
    composable(route = SearchNavRoute) {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        SearchScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = hiltViewModel(),
            uiState = rememberSearchBarState(searching = true, focusRequester, scrollProgress = 1f),
            onEnterExit = { if (!it) onBackClick() },
            onAnimeClick = onAnimeClick
        )
    }
}