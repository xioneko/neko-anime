package com.xioneko.android.nekoanime.ui.download

import androidx.annotation.OptIn
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.data.model.asAnimeShell
import com.xioneko.android.nekoanime.ui.component.ConfirmationDialog
import com.xioneko.android.nekoanime.ui.component.ExpandedAnimeCard
import com.xioneko.android.nekoanime.ui.component.LoadingDots
import com.xioneko.android.nekoanime.ui.component.NoResults
import com.xioneko.android.nekoanime.ui.component.SolidTopBar
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.util.getAspectRadio
import com.xioneko.android.nekoanime.ui.util.isTablet

@OptIn(UnstableApi::class)
@Composable
fun MyDownloadsScreen(
    viewModel: MyDownloadsViewModel = hiltViewModel(),
    onDownloadedAnimeClick: (AnimeShell) -> Unit,
    onBackClick: () -> Unit
) {
    val aspectRatio = getAspectRadio()
    val isTablet = isTablet()
    val context = LocalContext.current

    val downloadedAnime by viewModel.downloadedAnime.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SolidTopBar(
                title = "我的下载",
                rightIconId = NekoAnimeIcons.trash2,
                onLeftIconClick = onBackClick,
                onRightIconClick = { if (downloadedAnime?.isNotEmpty() == true) showDialog = true }
            )
        },
        containerColor = basicWhite
    ) { padding ->
        if (downloadedAnime == null) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
                content = { LoadingDots() }
            )
        } else if (downloadedAnime!!.isEmpty()) {
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
                items(downloadedAnime!!, { it.anime.id }) { item ->
                    ExpandedAnimeCard(
                        anime = item.anime.asAnimeShell(),
                        subTitle = "${item.numberOfDownloads} 个内容",
                        onClick = { a, b, c -> onDownloadedAnimeClick(item.anime.asAnimeShell()) }
                    )
                }
            }
        }

        if (showDialog) {
            ConfirmationDialog(
                text = "删除所有下载内容吗？",
                onConfirm = {
                    viewModel.removeAllDownloads(context)
                    showDialog = false
                },
                onDismiss = { showDialog = false }
            )
        }
    }
}