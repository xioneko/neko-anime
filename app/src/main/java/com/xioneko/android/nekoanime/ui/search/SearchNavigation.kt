package com.xioneko.android.nekoanime.ui.search

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navOptions

const val SearchNavRoute = "search_route"

fun NavGraphBuilder.searchScreen(
    onAnimeClick: (Int) -> Unit,
    onBackClick: () -> Unit,
) {
    composable(route = SearchNavRoute) {
        val focusRequester = remember { FocusRequester() }

        // 避免屏幕旋转导致搜索框被无意中 focused 并丢失搜索结果
        var shouldRequestFocus by rememberSaveable { mutableStateOf(true) }
        if (shouldRequestFocus) {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                shouldRequestFocus = false
            }
        }

        SearchScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = hiltViewModel(),
            uiState = rememberSearchBarState(searching = true, focusRequester, scrollProgress = 1f),
            onEnterExit = { if (!it) onBackClick() },
            onAnimeClick = onAnimeClick
        )
    }
}

fun NavHostController.navigateToSearchScreen() {
    navigate(SearchNavRoute, navOptions { launchSingleTop = true })
}