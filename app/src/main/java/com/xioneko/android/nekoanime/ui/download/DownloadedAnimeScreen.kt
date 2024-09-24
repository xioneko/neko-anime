@file:kotlin.OptIn(ExperimentalFoundationApi::class)

package com.xioneko.android.nekoanime.ui.download

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download.STATE_COMPLETED
import androidx.media3.exoplayer.offline.Download.STATE_DOWNLOADING
import androidx.media3.exoplayer.offline.Download.STATE_FAILED
import androidx.media3.exoplayer.offline.Download.STATE_QUEUED
import androidx.media3.exoplayer.offline.Download.STATE_STOPPED
import com.xioneko.android.nekoanime.data.AnimeDownloadHelper.Companion.FAILURE_REASON_SOURCE_ERROR
import com.xioneko.android.nekoanime.data.AnimeDownloadHelper.Companion.STATE_PREPARING
import com.xioneko.android.nekoanime.data.AnimeDownloadHelper.Companion.STOP_REASON_PAUSE
import com.xioneko.android.nekoanime.data.AnimeDownloadHelper.DownloadedAnime
import com.xioneko.android.nekoanime.ui.component.AnimatedCheckbox
import com.xioneko.android.nekoanime.ui.component.AnimeImage
import com.xioneko.android.nekoanime.ui.component.ConfirmationDialog
import com.xioneko.android.nekoanime.ui.component.LoadingDots
import com.xioneko.android.nekoanime.ui.component.NoResults
import com.xioneko.android.nekoanime.ui.component.SolidTopBar
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicBlack
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.theme.neutral01
import com.xioneko.android.nekoanime.ui.theme.neutral02
import com.xioneko.android.nekoanime.ui.theme.neutral05
import com.xioneko.android.nekoanime.ui.theme.neutral08
import com.xioneko.android.nekoanime.ui.theme.pink30
import com.xioneko.android.nekoanime.ui.theme.pink50
import kotlin.math.round

@OptIn(UnstableApi::class)
@Composable
fun DownloadedAnimeScreen(
    animeId: Int,
    animeName: String?,
    imageUrl: String?,
    onAnimeClick: (Int, Int) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel =
        hiltViewModel<DownloadedAnimeViewModel, DownloadedAnimeViewModel.Factory> { factory ->
            factory.create(animeId, animeName, imageUrl)
        }
    val anime by viewModel.anime.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    val lazyListState = rememberLazyListState()
    var showCheckboxes by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val items by viewModel.downloadedItems.collectAsStateWithLifecycle()
    val unselectedAllItems = {
        viewModel.unselectAll()
        showCheckboxes = false
    }

    DisposableEffect(Unit) {
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (showCheckboxes) {
                    unselectedAllItems()
                } else {
                    onBackClick()
                }
            }
        }
        backDispatcher?.addCallback(backCallback)

        onDispose {
            backCallback.remove()
        }
    }

    Scaffold(
        topBar = {
            SolidTopBar(
                title = anime.name,
                leftIconId = if (showCheckboxes) NekoAnimeIcons.close else NekoAnimeIcons.arrowLeft,
                onLeftIconClick = if (showCheckboxes) unselectedAllItems else onBackClick,
            )
        },
        containerColor = basicWhite
    ) { padding ->
        if (items == null) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
                content = { LoadingDots() }
            )
        } else if (items!!.isEmpty()) {
            NoResults(
                modifier = Modifier.fillMaxSize(),
                text = "这里什么都没有呢~"
            )
        } else {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState,
                    contentPadding = PaddingValues(0.dp),
                ) {
                    items(items!!, { it.episode }) { item ->
                        val progress = if (item.state !in listOf(STATE_COMPLETED, STATE_FAILED)) {
                            viewModel.progressFlow(context, item.episode)
                                .collectAsStateWithLifecycle()
                        } else null
                        DownloadAnimeItem(
                            item = item,
                            progress = progress?.value ?: 0f,
                            imgUrl = anime.imageUrl,
                            onAnimeClick = onAnimeClick,
                            selected = if (showCheckboxes) {
                                viewModel.selectedItems.containsKey(item.episode)
                            } else null,
                            onSelectedChange = {
                                viewModel.toggleSelection(item)
                                if (!showCheckboxes) {
                                    showCheckboxes = true
                                }
                            },
                            onPauseDownload = { viewModel.pauseDownload(context, it) },
                            onResumeDownload = { viewModel.resumeDownload(context, it) },
                            onRetry = { viewModel.retryDownload(context, it) }
                        )
                    }
                }
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    visible = showCheckboxes,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it },
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        color = basicWhite,
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        if (viewModel.selectedItems.size == items!!.size) {
                                            viewModel.unselectAll()
                                        } else {
                                            viewModel.selectAll()
                                        }
                                    }
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AnimatedCheckbox(
                                    modifier = Modifier.size(24.dp),
                                    checked = viewModel.selectedItems.size == items!!.size
                                )
                                Text(
                                    text = "全选",
                                    color = basicBlack,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { showDeleteConfirmDialog = true }
                                ),
                                text = "删除（${viewModel.selectedItems.size}）",
                                color = pink30,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
                if (showDeleteConfirmDialog) {
                    ConfirmationDialog(
                        text = "确定要删除吗？",
                        onConfirm = {
                            viewModel.removeSelected(context)
                            showCheckboxes = false
                            showDeleteConfirmDialog = false
                        },
                        onDismiss = { showDeleteConfirmDialog = false }
                    )
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun DownloadAnimeItem(
    modifier: Modifier = Modifier,
    imgUrl: String?,
    item: DownloadedAnime,
    progress: Float,
    selected: Boolean? = null,
    onSelectedChange: (Boolean) -> Unit,
    onPauseDownload: (DownloadedAnime) -> Unit,
    onResumeDownload: (DownloadedAnime) -> Unit,
    onRetry: (DownloadedAnime) -> Unit,
    onAnimeClick: (Int, Int) -> Unit,
) {
    Row(
        modifier = modifier
            .height(86.dp)
            .fillMaxWidth()
            .combinedClickable(
                enabled = true,
                onClick = {
                    if (selected == null) {
                        when (item.state) {
                            STATE_COMPLETED -> onAnimeClick(item.animeId, item.episode)
                            STATE_DOWNLOADING -> onPauseDownload(item)
                            STATE_STOPPED -> onResumeDownload(item)
                            STATE_FAILED -> onRetry(item)
                        }
                    } else {
                        onSelectedChange(!selected)
                    }
                },
                onLongClick = { if (selected == null) onSelectedChange(true) }
            )
            .padding(horizontal = 15.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimeImage(
            modifier = Modifier.weight(0.3f),
            imageUrl = imgUrl,
        )
        Column(
            modifier = Modifier
                .weight(0.7f)
                .padding(top = 4.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier,
                        text = "第${item.episode}话",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = basicBlack,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        InfoTag(
                            iconId = NekoAnimeIcons.status,
                            text = statusLabelOf(item)
                        )
                        if (selected == null) {
                            when (item.state) {
                                STATE_DOWNLOADING -> Text(
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.End,
                                    text = "%.1f%%".format(progress),
                                    color = neutral08,
                                    style = MaterialTheme.typography.labelSmall
                                )


                                STATE_COMPLETED -> Text(
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.End,
                                    text = "${round(item.bytesDownloaded / 1024f / 1024f).toInt()}MB",
                                    color = neutral08,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                    }
                }
                if (selected == null) {
                    when (item.state) {
                        STATE_STOPPED -> {
                            Icon(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .size(24.dp),
                                painter = painterResource(id = NekoAnimeIcons.play),
                                contentDescription = null,
                                tint = neutral05
                            )
                        }

                        STATE_FAILED -> {
                            Icon(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .size(24.dp),
                                painter = painterResource(id = NekoAnimeIcons.retry),
                                contentDescription = null,
                                tint = neutral05
                            )
                        }
                    }
                } else {
                    AnimatedCheckbox(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(24.dp),
                        checked = selected
                    )
                }

            }
            if (item.state == STATE_DOWNLOADING || item.state == STATE_STOPPED) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .requiredHeight(1.5.dp)
                        .fillMaxWidth(),
                    progress = { progress / 100f },
                    color = if (item.state == STATE_STOPPED) neutral02 else pink50.copy(0.5f),
                    trackColor = neutral01,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun InfoTag(modifier: Modifier = Modifier, iconId: Int, text: String) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(iconId),
            contentDescription = null,
            tint = neutral08
        )
        Text(
            text = text,
            color = neutral08,
            style = MaterialTheme.typography.labelSmall
        )
    }

}

@OptIn(UnstableApi::class)
private fun statusLabelOf(item: DownloadedAnime) =
    when (item.state) {
        STATE_QUEUED,
        STATE_PREPARING -> "正在开始"

        STATE_DOWNLOADING -> "下载中"

        STATE_FAILED -> when (item.failureReason) {
            FAILURE_REASON_SOURCE_ERROR -> "视频源错误"
            else -> "下载错误"
        }

        STATE_STOPPED -> when (item.stopReason) {
            STOP_REASON_PAUSE -> "已暂停"
            else -> "已停止"
        }

        STATE_COMPLETED -> "已完成"
        else -> "未知状态"
    }