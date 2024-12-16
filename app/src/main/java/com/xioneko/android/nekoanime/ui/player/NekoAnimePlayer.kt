package com.xioneko.android.nekoanime.ui.player

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.lanlinju.videoplayer.icons.Subtitles
import com.lanlinju.videoplayer.icons.SubtitlesOff
import com.xioneko.android.nekoanime.data.network.danmu.api.DanmuEvent
import com.xioneko.android.nekoanime.data.network.danmu.api.DanmuSession
import com.xioneko.android.nekoanime.data.network.danmu.dto.DanmakuPresentation
import com.xioneko.android.nekoanime.ui.component.LoadingDotsVariant
import com.xioneko.android.nekoanime.ui.danmu.DanmakuConfigData
import com.xioneko.android.nekoanime.ui.danmu.rememberDanmakuHostState
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeFontFamilies
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.theme.pink50
import com.xioneko.android.nekoanime.ui.util.KEY_DANMAKU_CONFIG_DATA
import com.xioneko.android.nekoanime.ui.util.KeepScreenOn
import com.xioneko.android.nekoanime.ui.util.currentScreenSizeDp
import com.xioneko.android.nekoanime.ui.util.getMediaVolume
import com.xioneko.android.nekoanime.ui.util.getScreenBrightness
import com.xioneko.android.nekoanime.ui.util.isTablet
import com.xioneko.android.nekoanime.ui.util.rememberPreference
import com.xioneko.android.nekoanime.ui.util.vibrate
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


@Composable
fun NekoAnimePlayer(
    player: ExoPlayer,
    uiState: AnimePlayUiState,
    episode: Int?,
    playerState: NekoAnimePlayerState,
    dragGestureState: DragGestureState,
    isFullscreen: Boolean,
    onEpisodeChange: (Int) -> Unit,
    onFullScreenChange: (Boolean) -> Unit,
    onDragGesture: (DragGestureEvent) -> Unit,
    onStartDownloadDrawerOpen: () -> Unit,
    onBack: () -> Unit,
    onDanmakuClick: (Boolean) -> Unit,
    enableDanmu: Boolean,
    danmuSession: DanmuSession?
) {
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val (screenWidthDp, _) = currentScreenSizeDp()
    var userPosition by remember { mutableLongStateOf(0L) }
    var fastForwarding by remember { mutableStateOf(false) }
    var isSeekBarDragging by remember { mutableStateOf(false) }
    val isDraggingProgress by remember(dragGestureState) {
        derivedStateOf {
            dragGestureState !is DragGestureState.None
        }
    }

    if (!isDraggingProgress && !isSeekBarDragging) {
        LaunchedEffect(Unit) {
            while (true) {
                userPosition = player.currentPosition
                delay(500)
            }
        }
    }

    if (!playerState.isPaused) {
        KeepScreenOn()
    }


    var isTopControllerVisible by remember(isFullscreen) { mutableStateOf(true) }
    var isBottomControllerVisible by remember(isFullscreen) { mutableStateOf(true) }
    var isEpisodesDrawerVisible by remember(isFullscreen) { mutableStateOf(false) }
    val view = LocalView.current
    val activity = LocalContext.current as Activity

    // 自动隐藏播放控件
    if (isBottomControllerVisible && !isDraggingProgress && !isSeekBarDragging) {
        LaunchedEffect(Unit) {
            delay(5.seconds)
            isBottomControllerVisible = false
            if (isFullscreen)
                isTopControllerVisible = false
        }
    }

    LaunchedEffect(isDraggingProgress) {
        if (isDraggingProgress) {
            isBottomControllerVisible = false // 避免多点触控
            if (isFullscreen) isTopControllerVisible = false  // 上下播放控件同步隐藏
        }
    }

    LaunchedEffect(fastForwarding) {
        if (fastForwarding) {
            player.setPlaybackSpeed(2f)
            isBottomControllerVisible = false
            if (isFullscreen) isTopControllerVisible = false
        } else {
            player.setPlaybackSpeed(1f)
        }
    }

    LaunchedEffect(isSeekBarDragging, userPosition) {
        if (isSeekBarDragging) {
            delay(300)
            player.seekTo(userPosition)
            isSeekBarDragging = false
        }
    }



    val playerModifier = remember(isFullscreen) {
        if (isFullscreen) {
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .adaptiveSize(isFullscreen, view, activity)
        } else {
            Modifier
                .wrapContentHeight(Alignment.Top)
                .fillMaxWidth()
                .background(Color.Black)
                .statusBarsPadding()
                .aspectRatio(16f / 9f)
        }
    }
    var showTopMenu by remember { mutableStateOf(false) }

    Box(modifier = playerModifier) {
        if (playerState.isLoading) {
            LoadingDotsVariant(
                Modifier
                    .zIndex(1f)
                    .align(Alignment.Center)
            )
        }
        //TODO 仿照写的DanmakuHost被重复触发,导致session不断被重组,弹幕无法显示...不知道什么原因
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.TopCenter),
            visible = isFullscreen,
            enter = slideInVertically(spring()) { -it },
            exit = slideOutVertically(spring()) { -it }
        ) {
            DanmakuHost(playerState, danmuSession, enableDanmu)
        }

        AndroidView(
            modifier = Modifier
                .pointerInput(isFullscreen) {
                    detectTapGestures(
                        onTap = { // 点击屏幕显示/隐藏控件
                            isBottomControllerVisible = !isBottomControllerVisible
                            if (isFullscreen)
                                isTopControllerVisible = !isTopControllerVisible
                        },
                        onDoubleTap = { // 双击屏幕播放/暂停
                            player.playWhenReady = !player.playWhenReady
                            if (!player.playWhenReady) {
                                isBottomControllerVisible = true
                                isTopControllerVisible = true
                            }
                        },
                    )
                }
                .pointerInput(player.isPlaying) {
                    if (player.isPlaying) {
                        detectDragGesturesAfterLongPress( // 长按屏幕快进
                            onDragStart = {
                                vibrate(context)
                                fastForwarding = true
                            },
                            onDragEnd = { fastForwarding = false },
                            onDragCancel = { fastForwarding = false },
                            onDrag = { _, _ -> }
                        )
                    }
                }
                .pointerInput(player.duration > 0, isFullscreen) { // 水平滑动改变播放进度
                    if (player.duration > 0) {
                        val threshold = density / 2
                        val upperBound = if (isFullscreen) 30 * density else 0f

                        var isDragStart = false
                        var positionOffset = 0L
                        var startPosition: Long? = null

                        detectHorizontalDragGestures(
                            onDragEnd = {
                                onDragGesture(DragGestureEvent.End)
                                positionOffset = 0L
                                startPosition = null
                                isDragStart = false
                            },
                            onHorizontalDrag = cb@{ change, dx ->
                                val y = change.position.y

                                if (!isDragStart) {
                                    isDragStart = true
                                    if (y >= upperBound && abs(dx) > threshold) {
                                        startPosition = player.currentPosition
                                        onDragGesture(
                                            DragGestureEvent.Start(
                                                DragType.Progress,
                                                startPosition!!
                                            )
                                        )
                                    } else return@cb
                                }
                                if (startPosition == null) return@cb

                                positionOffset += dx.toLong() * 250
                                val newPosition =
                                    (startPosition!! + positionOffset)
                                        .coerceIn(0, player.duration)
                                onDragGesture(DragGestureEvent.Update(newPosition))
                            },
                        )
                    }
                }
                .pointerInput(player.duration > 0, isFullscreen) { // 垂直滑动改变音量或亮度
                    if (player.duration > 0) {
                        val threshold = density / 2
                        val width = screenWidthDp * density
                        val leftBound = width / 2.5
                        val rightBound = width / 1.25
                        val upperBound = if (isFullscreen) 30 * density else 0f

                        var isDragStart = false
                        var dragType: DragType? = null
                        var offset: Float? = null
                        var startValue = 0f

                        detectVerticalDragGestures(
                            onDragEnd = {
                                dragType = null
                                offset = null
                                startValue = 0f
                                isDragStart = false
                                onDragGesture(DragGestureEvent.End)
                            },
                            onVerticalDrag = cb@{ change, dy ->
                                val (x, y) = change.position

                                if (!isDragStart) {
                                    isDragStart = true
                                    if (y >= upperBound && abs(dy) > threshold) offset = 0f
                                    else return@cb
                                }
                                if (offset == null) return@cb


                                val currDragType = when {
                                    x <= leftBound -> DragType.Brightness
                                    x >= rightBound -> DragType.Volume
                                    else -> null
                                }

                                if (currDragType != null) {
                                    if (dragType == null) {
                                        dragType = currDragType
                                        startValue = if (currDragType == DragType.Brightness)
                                            context.getScreenBrightness()
                                        else context.getMediaVolume()

                                        onDragGesture(
                                            DragGestureEvent.Start(currDragType, startValue)
                                        )
                                    } else if (dragType != currDragType) {
                                        offset = null
                                        onDragGesture(DragGestureEvent.End)
//                                        Log.d("Player", "verticalDrag Conflict")
                                        return@cb
                                    }
                                }

                                if (dragType != null) {
                                    offset = offset!! - dy / 1000f
                                    val newValue = (startValue + offset!!).coerceIn(0f, 1f)
                                    onDragGesture(DragGestureEvent.Update(newValue))
                                }
                            }
                        )
                    }
                },
            factory = { context ->
                PlayerView(context).apply {
                    useController = false
                    this.player = player
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            })

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.TopCenter),
            visible = isTopControllerVisible,
            enter = slideInVertically(spring()) { -it },
            exit = slideOutVertically(spring()) { -it }
        ) {
            TopController(
                modifier = Modifier.background(
                    Brush.verticalGradient(listOf(Color.Black.copy(0.5f), Color.Transparent))
                ),
                title = if (uiState is AnimePlayUiState.Data)
                    "${uiState.anime.name} 第${episode}话" else "",
                isFullscreen = isFullscreen,
                showMenu = showTopMenu,
                onBack = onBack,
                onDownload = onStartDownloadDrawerOpen,
                toggleMenu = { showTopMenu = !showTopMenu }
            )
        }

        if (isDraggingProgress) {
            with(dragGestureState as DragGestureState.Data) {
                when (type) {
                    DragType.Progress -> PlayProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        position = value.toLong(),
                        duration = player.duration
                    )

                    DragType.Volume,
                    DragType.Brightness -> DefaultProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        progress = value.toFloat(),
                        iconId = when (type) {
                            DragType.Volume -> if (value.toFloat() > 0)
                                NekoAnimeIcons.Player.volume
                            else NekoAnimeIcons.Player.volumeMute

                            else -> NekoAnimeIcons.Player.brightness
                        }
                    )
                }
            }
        }

        if (fastForwarding) {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(0.5f))
                    .padding(12.dp, 8.dp)
            ) {
                val transition = rememberInfiniteTransition(label = "fastForward")

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val color by transition.animateColor(
                            initialValue = Color.LightGray.copy(alpha = 0.1f),
                            targetValue = Color.LightGray,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 600, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse,
                                initialStartOffset = StartOffset(index * 200)
                            ),
                            label = "color",
                        )
                        Text(
                            text = "▶",
                            color = color,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Text(
                        modifier = Modifier.padding(start = 6.dp),
                        text = "倍速播放中",
                        color = basicWhite.copy(0.8f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }


        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = isBottomControllerVisible,
            enter = slideInVertically(spring()) { it },
            exit = slideOutVertically(spring()) { it }
        ) {
            BottomController(
                modifier = Modifier.background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.5f)))
                ),
                isPaused = playerState.isPaused,
                currentEpisode = episode ?: 1,
                totalEpisodes = if (uiState is AnimePlayUiState.Data) uiState.anime.latestEpisode else 1,
                currentPosition = userPosition,
                totalDurationMs = playerState.totalDurationMs,
                bufferedPercentage = playerState.bufferedPercentage,
                isFullscreen = isFullscreen,
                onPlay = player::play,
                onPause = player::pause,
                onFullScreen = { onFullScreenChange(true) },
                onPositionChange = {
                    if (player.duration > 0) {
                        userPosition = it
                        isSeekBarDragging = true
                    }
                },
                onEpisodeChange = onEpisodeChange,
                showEpisodesDrawer = {
                    isBottomControllerVisible = false
                    isTopControllerVisible = false
                    isEpisodesDrawerVisible = true
                },
                onDanmukuClick = onDanmakuClick,
                enabledDanmuku = enableDanmu
            )
        }

        AnimatedVisibility(
            visible = isEpisodesDrawerVisible,
            enter = slideInHorizontally { it / 2 },
            exit = slideOutHorizontally { it / 2 }
        ) {
            EpisodesDrawer(
                currentEpisode = episode ?: 1,
                totalEpisodes = if (uiState is AnimePlayUiState.Data) uiState.anime.latestEpisode else 1,
                onEpisodeChange = {
                    onEpisodeChange(it)
                    isEpisodesDrawerVisible = false
                },
                hideDrawer = {
                    isBottomControllerVisible = true
                    isTopControllerVisible = true
                    isEpisodesDrawerVisible = false
                }
            )
        }
    }
}

@Composable
private fun TopController(
    modifier: Modifier = Modifier,
    title: String,
    isFullscreen: Boolean,
    showMenu: Boolean,
    onBack: () -> Unit,
    onDownload: () -> Unit,
    toggleMenu: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .displayCutoutPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onBack
                    )
                    .padding(top = 15.dp, start = 15.dp, bottom = 15.dp),
                painter = painterResource(NekoAnimeIcons.Player.back),
                contentDescription = "back",
                tint = basicWhite
            )
            if (isFullscreen)
            // TODO: 文字溢出，自动滚动
                Text(
                    text = title,
                    maxLines = 1,
                    color = basicWhite,
                    style = MaterialTheme.typography.bodyMedium
                )
        }
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(
                modifier = Modifier.padding(end = 15.dp),
                visible = showMenu,
                enter = fadeIn() + slideInHorizontally { it / 2 },
                exit = fadeOut() + slideOutHorizontally { it / 2 }
            ) {
                Icon(
                    modifier =
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onDownload
                    ),
                    painter = painterResource(NekoAnimeIcons.Player.download),
                    contentDescription = "download",
                    tint = basicWhite
                )
            }
            Icon(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = toggleMenu
                    )
                    .padding(top = 15.dp, end = 15.dp, bottom = 15.dp),
                painter = painterResource(NekoAnimeIcons.Player.more),
                contentDescription = "menu",
                tint = basicWhite
            )
        }
    }
}

@Composable
private fun BottomController(
    modifier: Modifier = Modifier,
    isPaused: Boolean,
    enabledDanmuku: Boolean,
    currentEpisode: Int,
    totalEpisodes: Int,
    currentPosition: Long,
    totalDurationMs: Long,
    bufferedPercentage: Int,
    isFullscreen: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onFullScreen: () -> Unit,
    onPositionChange: (Long) -> Unit,
    onEpisodeChange: (Int) -> Unit,
    showEpisodesDrawer: () -> Unit,
    onDanmukuClick: (Boolean) -> Unit,
) {
    val totalDuration = remember(totalDurationMs) { formatMilliseconds(totalDurationMs) }
    val currentPos = remember(currentPosition) {
        formatMilliseconds(
            currentPosition,
            includeHour = totalDuration.length > 5
        )
    }
    if (isFullscreen) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .displayCutoutPadding()
                .padding(vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentPos,
                    color = basicWhite,
                    fontFamily = NekoAnimeFontFamilies.robotoFamily,
                    style = MaterialTheme.typography.labelSmall
                )
                SeekBar(
                    modifier = Modifier.weight(1f),
                    currentPosition = currentPosition,
                    totalDurationMs = totalDurationMs,
                    bufferedPercentage = bufferedPercentage,
                    onPositionChange = onPositionChange,
                )
                Text(
                    text = totalDuration,
                    color = basicWhite,
                    fontFamily = NekoAnimeFontFamilies.robotoFamily,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    AnimatedPlayPauseButton(
                        modifier = Modifier.scale(1.2f),
                        isPaused = isPaused,
                        onPlay = onPlay,
                        onPause = onPause
                    )
                    Icon(
                        modifier = Modifier.clickable(
                            enabled = currentEpisode < totalEpisodes,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = { onEpisodeChange(currentEpisode + 1) }
                        ),
                        painter = painterResource(NekoAnimeIcons.Player.playNext),
                        contentDescription = "play next",
                        tint = if (currentEpisode < totalEpisodes) basicWhite
                        else basicWhite.copy(0.6f)
                    )
                    //TODo  加入弹幕开关
                    DanmakuIcon(onClick = onDanmukuClick, danmakuEnabled = enabledDanmuku)

                }
                Text(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.DropdownList,
                        onClick = showEpisodesDrawer
                    ),
                    text = "选集",
                    color = basicWhite,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(12.dp, 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedPlayPauseButton(
                isPaused = isPaused,
                onPlay = onPlay,
                onPause = onPause
            )
            SeekBar(
                modifier = Modifier.weight(1f),
                currentPosition = currentPosition,
                totalDurationMs = totalDurationMs,
                bufferedPercentage = bufferedPercentage,
                onPositionChange = onPositionChange,
            )
            Text(
                text = "$currentPos / $totalDuration",
                color = basicWhite,
                fontFamily = NekoAnimeFontFamilies.robotoFamily,
                style = MaterialTheme.typography.labelSmall
            )
            Icon(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = onFullScreen
                ),
                painter = painterResource(NekoAnimeIcons.Player.expand),
                contentDescription = "full-screen",
                tint = basicWhite
            )
        }
    }
}

private val MediumIconButtonSize = 42.dp

@Composable
private fun DanmakuIcon(
    danmakuEnabled: Boolean,
    onClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AdaptiveIconButton(
        onClick = { onClick(!danmakuEnabled) },
        modifier.size(MediumIconButtonSize),
    ) {
        if (danmakuEnabled) {
            Icon(Icons.Rounded.Subtitles, contentDescription = "禁用弹幕")
        } else {
            Icon(Icons.Rounded.SubtitlesOff, contentDescription = "启用弹幕")
        }
    }
}

@Composable
private fun AdaptiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabledIndication: Boolean = true,
    content: @Composable () -> Unit
) {
    val indication = LocalIndication.current

    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource,
                indication = if (enabledIndication) indication else null
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}


@Composable
private fun AnimatedPlayPauseButton(
    modifier: Modifier = Modifier,
    isPaused: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
) {
    AnimatedContent(
        targetState = isPaused,
        transitionSpec = {
            (scaleIn(
                tween(delayMillis = 100),
                0.8f
            ) + fadeIn(tween(delayMillis = 100))).togetherWith(
                scaleOut(targetScale = 0.7f) + fadeOut()
            )
        }, label = ""
    ) { paused ->
        val rotation by transition.animateFloat(
            transitionSpec = { tween(400) },
            label = ""
        ) {
            when (it) {
                EnterExitState.PreEnter -> -55f
                EnterExitState.Visible -> 0f
                EnterExitState.PostExit -> 30f
            }
        }
        if (paused) {
            Icon(
                modifier = modifier
                    .rotate(rotation)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Switch,
                        onClick = onPlay
                    ),
                painter = painterResource(NekoAnimeIcons.Player.play),
                contentDescription = "play",
                tint = basicWhite
            )
        } else {
            Icon(
                modifier = modifier
                    .rotate(rotation)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Switch,
                        onClick = onPause
                    ),
                painter = painterResource(NekoAnimeIcons.Player.pause),
                contentDescription = "pause",
                tint = basicWhite
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeekBar(
    modifier: Modifier = Modifier,
    currentPosition: Long,
    bufferedPercentage: Int,
    totalDurationMs: Long,
    onPositionChange: (Long) -> Unit,
) {
    Box(
        modifier = modifier
            .requiredHeight(24.dp)
            .clipToBounds()
            .padding(start = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = bufferedPercentage.toFloat(),
            onValueChange = {},
            enabled = false,
            valueRange = 0f..100f,
            track = {
                SliderDefaults.Track(
                    modifier = Modifier
                        .requiredHeight(2.dp)
                        .clipToBounds(),
                    sliderState = it,
                    enabled = false,
                    colors = SliderDefaults.colors(
                        disabledActiveTrackColor = basicWhite.copy(0.4f),
                        disabledInactiveTrackColor = Color.Transparent
                    ),
                )
            },
            thumb = {}
        )
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = currentPosition.toFloat(),
            onValueChange = { onPositionChange(it.toLong()) },
            valueRange = 0f..totalDurationMs.toFloat(),
            thumb = {
                Image(
                    modifier = Modifier
                        .scale(1.2f)
                        .offset(y = 4.dp),
                    painter = painterResource(NekoAnimeIcons.Player.thumb),
                    contentDescription = "thumb"
                )
            },
            track = {
                SliderDefaults.Track(
                    modifier = Modifier
                        .requiredHeight(2.dp)
                        .clip(CircleShape),
                    sliderState = it,
                    colors = SliderDefaults.colors(
                        activeTrackColor = pink50.copy(0.85f),
                        inactiveTrackColor = basicWhite.copy(0.25f)
                    )
                )
            }
        )
    }
}

@Composable
private fun AnimatedVisibilityScope.EpisodesDrawer(
    currentEpisode: Int,
    totalEpisodes: Int,
    onEpisodeChange: (Int) -> Unit,
    hideDrawer: () -> Unit,
) {
    val isTablet = isTablet()

    Box(
        modifier = Modifier.displayCutoutPadding(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .animateEnterExit(enter = fadeIn(), exit = fadeOut())
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = hideDrawer
                )
                .background(
                    Brush.horizontalGradient(
                        0f to Color.Black.copy(0f),
                        1f to Color.Black.copy(0.9f)
                    )
                ),
        )
        Column(
            modifier = Modifier
                .width(min(360.dp, LocalConfiguration.current.screenWidthDp.dp * 4 / 5))
                .padding(start = 15.dp, end = 15.dp, top = 15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val cellSize = if (isTablet) 64.dp else 50.dp
            Text(
                text = "选集",
                color = basicWhite.copy(0.65f),
                style = MaterialTheme.typography.bodyLarge,
            )
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(cellSize),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(totalEpisodes) {
                    val episode = it + 1
                    item(episode) {
                        if (episode == currentEpisode) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(cellSize)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.LightGray.copy(0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = episode.toString(),
                                    color = basicWhite,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .requiredSize(cellSize)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.LightGray.copy(0.1f))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        role = Role.Button,
                                        onClick = { onEpisodeChange(episode) }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = episode.toString(),
                                    color = basicWhite,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayProgressIndicator(
    modifier: Modifier = Modifier,
    position: Long,
    duration: Long,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(0.3f))
            .padding(12.dp, 9.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = basicWhite)) {
                        append(formatMilliseconds(position))
                    }
                    withStyle(style = SpanStyle(color = basicWhite.copy(0.6f))) {
                        append(" / ")
                        append(formatMilliseconds(duration))
                    }
                },
                style = MaterialTheme.typography.titleMedium,
                fontFamily = NekoAnimeFontFamilies.robotoFamily,
                fontWeight = FontWeight.Bold,
            )

            LinearProgressIndicator(
                modifier = Modifier
                    .requiredHeight(2.dp)
                    .padding(end = 8.dp)
                    .width(100.dp),
                progress = { position.toFloat() / duration },
                color = pink50,
                trackColor = basicWhite.copy(0.25f),
                strokeCap = StrokeCap.Round
            )
        }

    }
}

@Composable
private fun DefaultProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float,
    iconId: Int,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(0.3f))
            .padding(6.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(36.dp),
                painter = painterResource(iconId),
                contentDescription = "icon",
                tint = basicWhite.copy(0.8f)
            )
            LinearProgressIndicator(
                modifier = Modifier
                    .requiredHeight(2.dp)
                    .padding(end = 8.dp)
                    .width(100.dp),
                progress = { progress },
                color = pink50,
                trackColor = basicWhite.copy(0.25f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

private fun formatMilliseconds(millis: Long, includeHour: Boolean? = null): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)

    val useHours = includeHour ?: (hours > 0)

    return if (useHours) {
        String.format(
            Locale.CHINA,
            "%d:%02d:%02d",
            hours,
            minutes - hours * 60,
            seconds - minutes * 60
        )
    } else {
        String.format(Locale.CHINA, "%02d:%02d", minutes, seconds - minutes * 60)
    }
}


private fun Modifier.adaptiveSize(
    fullscreen: Boolean,
    view: View,
    activity: Activity
): Modifier {
    return if (fullscreen) {
        requestLandscapeOrientation(view, activity)
        fillMaxSize()
    } else {
        fillMaxWidth().aspectRatio(1.778f)
    }
}

private fun requestLandscapeOrientation(view: View, activity: Activity) {
    hideSystemBars(view, activity)

    if (isWideScreen(activity)) return

    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
}


private fun hideSystemBars(view: View, activity: Activity) {
    val windowInsetsController = WindowCompat.getInsetsController(activity.window, view)
    // Configure the behavior of the hidden system bars
    windowInsetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    // Hide both the status bar and the navigation bar
    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
}

fun isWideScreen(context: Context): Boolean {
    val configuration = context.resources.configuration
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    return screenWidthDp > screenHeightDp
}

@Composable
fun DanmakuHost(
    playerState: NekoAnimePlayerState,
    session: DanmuSession?,
    enabled: Boolean
) {
    if (!enabled) return
    val danmakuConfigData by rememberPreference(
        KEY_DANMAKU_CONFIG_DATA,
        DanmakuConfigData(),
        DanmakuConfigData.serializer()
    )
    val danmakuHostState =
        rememberDanmakuHostState(danmakuConfig = danmakuConfigData.toDanmakuConfig())

    if (session != null) {
        Log.d("danmu", "弹幕流")
        com.xioneko.android.nekoanime.ui.danmu.DanmakuHost(state = danmakuHostState)
    }

    LaunchedEffect(playerState.isPlaying) {
        if (playerState.isPlaying) {
            danmakuHostState.play()
        } else {
            danmakuHostState.pause()
        }
    }

    val isPlayingFlow = remember { snapshotFlow { playerState.isPlaying } }
    LaunchedEffect(session) {
        danmakuHostState.clearPresentDanmaku()
        Log.d("danmu", "弹幕重置")
        session?.at(
            curTimeMillis = { playerState.position.milliseconds },
            isPlayingFlow = isPlayingFlow,
        )?.collect { danmakuEvent ->
            when (danmakuEvent) {
                is DanmuEvent.Add -> {
                    danmakuHostState.trySend(
                        DanmakuPresentation(
                            danmakuEvent.danmu,
                            false
                        )
                    )
                }
                // 快进/快退
                is DanmuEvent.Repopulate -> danmakuHostState.repopulate()
            }
        }
    }
}