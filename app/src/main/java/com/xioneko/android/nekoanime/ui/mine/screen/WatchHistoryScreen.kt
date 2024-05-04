package com.xioneko.android.nekoanime.ui.mine.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xioneko.android.nekoanime.ui.component.ConfirmationDialog
import com.xioneko.android.nekoanime.ui.component.LoadingDots
import com.xioneko.android.nekoanime.ui.component.NarrowAnimeCard
import com.xioneko.android.nekoanime.ui.component.NoResults
import com.xioneko.android.nekoanime.ui.component.PlaceholderAnimeCard
import com.xioneko.android.nekoanime.ui.component.SolidTopBar
import com.xioneko.android.nekoanime.ui.mine.MineScreenViewModel
import com.xioneko.android.nekoanime.ui.mine.WatchPeriod
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.util.getAspectRadio
import com.xioneko.android.nekoanime.ui.util.isTablet

@Composable
fun WatchHistoryScreen(
    viewModel: MineScreenViewModel = hiltViewModel(),
    onAnimeClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val aspectRatio = getAspectRadio()
    val isTablet = isTablet()

    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    var shouldConfirmRecordsCleanup by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SolidTopBar(
                title = "历史观看",
                onLeftIconClick = onBackClick,
                rightIconId = NekoAnimeIcons.trash2,
                onRightIconClick = {
                    if (watchHistory?.isNotEmpty() == true) shouldConfirmRecordsCleanup = true
                }
            )
        },
        containerColor = basicWhite,
    ) { padding ->
        if (watchHistory == null) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
                content = { LoadingDots() }
            )
        } else if (watchHistory!!.isEmpty()) {
            NoResults(
                modifier = Modifier.fillMaxSize(),
                text = "这里什么都没有呢~"
            )
        } else {
            LazyVerticalGrid(
                modifier = Modifier.padding(padding),
                columns = GridCells.Fixed(if (isTablet) 4 else if (aspectRatio < 0.56) 6 else 3),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                for (period in WatchPeriod.entries) {
                    watchHistory!![period]?.let { records ->
                        item(key = period.title, span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = period.title,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        items(records, { it.key }) {
                            val anime by viewModel.getAnimeById(it.key)
                                .collectAsStateWithLifecycle(null)
                            if (anime == null) {
                                PlaceholderAnimeCard()
                            } else {
                                NarrowAnimeCard(
                                    anime = anime!!,
                                    subTitle = "看过第${it.value.recentEpisode}话",
                                    onClick = onAnimeClick
                                )
                            }
                        }
                    }
                }
            }
        }

        if (shouldConfirmRecordsCleanup) {
            ConfirmationDialog(
                text = "清空所有观看记录吗？",
                onConfirm = {
                    viewModel.clearWatchRecords()
                    shouldConfirmRecordsCleanup = false
                },
                onDismiss = { shouldConfirmRecordsCleanup = false }
            )
        }
    }
}