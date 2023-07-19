package com.xioneko.android.nekoanime.ui.mine.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xioneko.android.nekoanime.ui.component.ConfirmationDialog
import com.xioneko.android.nekoanime.ui.component.NarrowAnimeCard
import com.xioneko.android.nekoanime.ui.component.PlaceholderAnimeCard
import com.xioneko.android.nekoanime.ui.component.SolidTopBar
import com.xioneko.android.nekoanime.ui.mine.MineScreenViewModel
import com.xioneko.android.nekoanime.ui.mine.WatchPeriod
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicWhite

@Composable
fun WatchHistoryScreen(
    viewModel: MineScreenViewModel = hiltViewModel(),
    onAnimeClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    var shouldConfirmRecordsCleanup by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SolidTopBar(
                title = "历史观看",
                onLeftIconClick = onBackClick,
                rightIconId = NekoAnimeIcons.trash2,
                onRightIconClick = {
                    if (watchHistory.isNotEmpty()) shouldConfirmRecordsCleanup = true
                }
            )
        },
        containerColor = basicWhite,
    ) {
        LazyVerticalGrid(
            modifier = Modifier.padding(it),
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            for (period in WatchPeriod.values()) {
                watchHistory[period]?.let { records ->
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