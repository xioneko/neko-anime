package com.xioneko.android.nekoanime.ui.mine.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xioneko.android.nekoanime.ui.component.FollowedAnimeCard
import com.xioneko.android.nekoanime.ui.component.LoadingDots
import com.xioneko.android.nekoanime.ui.component.NoResults
import com.xioneko.android.nekoanime.ui.component.SolidTopBar
import com.xioneko.android.nekoanime.ui.mine.MineScreenViewModel
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.util.getAspectRadio
import com.xioneko.android.nekoanime.ui.util.isTablet

@Composable
fun FollowedAnimeScreen(
    viewModel: MineScreenViewModel = hiltViewModel(),
    onAnimeClick: (Int, Int?, String?) -> Unit,
    onBackClick: () -> Unit
) {
    val aspectRatio = getAspectRadio()
    val isTablet = isTablet()

    val followedAnime by viewModel.followedAnime.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SolidTopBar(
                title = "我的追番",
                onLeftIconClick = onBackClick,
            )
        },
        containerColor = basicWhite
    ) { padding ->

        if (followedAnime == null)
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
                content = { LoadingDots() }
            )
        else if (followedAnime!!.isEmpty()) {
            NoResults(
                modifier = Modifier.fillMaxSize(),
                text = "你还没有追番哦~"
            )
        } else {
            LazyVerticalGrid(
                modifier = Modifier.padding(padding),
                columns = GridCells.Fixed(if (isTablet) 4 else if (aspectRatio < 0.56) 6 else 3),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(followedAnime!!, { it.id }) {
                    FollowedAnimeCard(anime = it, onClick = onAnimeClick)
                }
            }
        }
    }
}