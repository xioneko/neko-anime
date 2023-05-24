package com.xioneko.android.nekoanime.ui.search

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
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
        SearchScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = hiltViewModel(),
            uiState = rememberSearchBarState(searching = true, scrollProgress = 1f),
            onEnterExit = { if (!it) onBackClick() },
            onAnimeClick = onAnimeClick
        )
    }
}