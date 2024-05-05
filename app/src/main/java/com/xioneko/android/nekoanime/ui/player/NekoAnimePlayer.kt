package com.xioneko.android.nekoanime.ui.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.xioneko.android.nekoanime.ui.component.LoadingDotsVariant
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeFontFamilies
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.util.KeepScreenOn
import com.xioneko.android.nekoanime.ui.util.isTablet
import com.xioneko.android.nekoanime.ui.util.vibrate
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds


@Composable
fun NekoAnimePlayer(
    player: ExoPlayer,
    uiState: AnimePlayUiState,
    playerState: NekoAnimePlayerState,
    progressDragState: ProgressDragState,
    isFullscreen: Boolean,
    onEpisodeChange: (Int) -> Unit,
    onFullScreenChange: (Boolean) -> Unit,
    onProgressDrag: (ProgressDragEvent) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var userPosition by remember { mutableLongStateOf(0L) }
    var fastForwarding by remember { mutableStateOf(false) }

    if (!progressDragState.isDragging) {
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

    // 自动隐藏播放控件
    if (isBottomControllerVisible && !progressDragState.isDragging) {
        LaunchedEffect(Unit) {
            delay(5.seconds)
            isBottomControllerVisible = false
            if (isFullscreen)
                isTopControllerVisible = false
        }
    }

    LaunchedEffect(progressDragState.isDragging) {
        if (progressDragState.isDragging) {
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


    val playerModifier = remember(isFullscreen) {
        if (isFullscreen) {
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        } else {
            Modifier
                .wrapContentHeight(Alignment.Top)
                .fillMaxWidth()
                .background(Color.Black)
                .statusBarsPadding()
                .aspectRatio(16f / 9f)
        }
    }

    Box(playerModifier) {
        if (playerState.isLoading) {
            LoadingDotsVariant(
                Modifier
                    .zIndex(1f)
                    .align(Alignment.Center)
            )
        }
        AndroidView(
            modifier = Modifier
                .pointerInput(isFullscreen) {
                    detectTapGestures(
                        onTap = {
                            isBottomControllerVisible = !isBottomControllerVisible
                            if (isFullscreen)
                                isTopControllerVisible = !isTopControllerVisible
                        },
                        onDoubleTap = {
                            player.playWhenReady = !player.playWhenReady
                            if (!player.playWhenReady) {
                                isBottomControllerVisible = true
                                isTopControllerVisible = true
                            }
                        },
                    )
                }
                .pointerInput(player.duration) {
                    if (player.duration > 0) {
                        var positionOffset = 0L
                        var startPosition = 0L
                        detectHorizontalDragGestures(
                            onDragStart = {
                                onProgressDrag(ProgressDragEvent.Start)
                                startPosition = player.currentPosition
                            },
                            onDragEnd = {
                                onProgressDrag(ProgressDragEvent.End)
                                positionOffset = 0L
                                startPosition = 0L
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                positionOffset += dragAmount.toLong() * 500
                                val newPosition =
                                    (startPosition + positionOffset).coerceIn(0, player.duration)
                                onProgressDrag(ProgressDragEvent.Update(newPosition))
                            },
                        )
                    }
                }
                .pointerInput(player.isPlaying) {
                    if (player.isPlaying) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                vibrate(context)
                                fastForwarding = true
                            },
                            onDragEnd = {
                                fastForwarding = false
                            },
                            onDragCancel = {
                                fastForwarding = false
                            },
                            onDrag = { _, _ -> }
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
                    "${uiState.anime.name} 第${uiState.episode.value}话" else "",
                isFullscreen = isFullscreen,
                onBack = onBack
            )
        }

        if (progressDragState.isDragging) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(0.5f))
                    .padding(12.dp, 9.dp),
            ) {
                val position =
                    formatMilliseconds(progressDragState.endPosition)
                val duration = formatMilliseconds(player.duration)
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = basicWhite)) {
                            append(position)
                        }
                        withStyle(style = SpanStyle(color = basicWhite.copy(0.6f))) {
                            append(" / ")
                            append(duration)
                        }
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = NekoAnimeFontFamilies.robotoFamily,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        if (fastForwarding) {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(0.5f))
                    .padding(12.dp, 6.dp)
            ) {
                Text(
                    text = "▶▷▶ 倍速播放中",
                    color = basicWhite.copy(0.8f),
                    style = MaterialTheme.typography.bodySmall,
                )
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
                currentEpisode = if (uiState is AnimePlayUiState.Data) uiState.episode.value else 1,
                totalEpisodes = if (uiState is AnimePlayUiState.Data) uiState.anime.latestEpisode else 1,
                currentPosition = userPosition,
                totalDurationMs = playerState.totalDurationMs,
                bufferedPercentage = playerState.bufferedPercentage,
                isFullscreen = isFullscreen,
                onPlay = player::play,
                onPause = player::pause,
                onFullScreen = { onFullScreenChange(true) },
                onPositionChange = {
                    if (!progressDragState.isDragging) onProgressDrag(ProgressDragEvent.Start)
                    userPosition = it
                    onProgressDrag(ProgressDragEvent.Update(it))
                },
                onPositionChangeFinished = {
                    onProgressDrag(ProgressDragEvent.End)
                },
                onEpisodeChange = onEpisodeChange,
                showEpisodesDrawer = {
                    isBottomControllerVisible = false
                    isTopControllerVisible = false
                    isEpisodesDrawerVisible = true
                }
            )
        }

        AnimatedVisibility(
            visible = isEpisodesDrawerVisible,
            enter = slideInHorizontally { it / 2 },
            exit = slideOutHorizontally { it / 2 }
        ) {
            EpisodesDrawer(
                currentEpisode = if (uiState is AnimePlayUiState.Data) uiState.episode.value else 1,
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
    onBack: () -> Unit,
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
        Icon(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = { /* TODO: menu */ }
                )
                .padding(top = 15.dp, end = 15.dp, bottom = 15.dp),
            painter = painterResource(NekoAnimeIcons.Player.more),
            contentDescription = "menu",
//            tint = basicWhite
            tint = Color.Transparent // TODO: change color
        )
    }
}

@Composable
private fun BottomController(
    modifier: Modifier = Modifier,
    isPaused: Boolean,
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
    onPositionChangeFinished: (Long) -> Unit,
    onEpisodeChange: (Int) -> Unit,
    showEpisodesDrawer: () -> Unit,
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
                    onPositionChangeFinished = onPositionChangeFinished
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
                onPositionChangeFinished = onPositionChangeFinished
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

@OptIn(ExperimentalAnimationApi::class)
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
    onPositionChangeFinished: (Long) -> Unit,
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
            onValueChangeFinished = { onPositionChangeFinished(currentPosition) },
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
                        activeTrackColor = basicWhite.copy(0.8f),
                        inactiveTrackColor = basicWhite.copy(0.25f)
                    )
                )
            }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
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