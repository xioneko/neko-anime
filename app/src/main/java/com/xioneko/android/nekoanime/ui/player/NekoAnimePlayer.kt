package com.xioneko.android.nekoanime.ui.player

import android.util.Log
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
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.xioneko.android.nekoanime.ui.component.LoadingDotsVariant
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicBlack
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.util.KeepScreenOn
import com.xioneko.android.nekoanime.ui.util.isTablet
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds


@Composable
fun NekoAnimePlayer(
    modifier: Modifier = Modifier,
    player: ExoPlayer,
    uiState: AnimePlayUiState,
    playerState: NekoAnimePlayerState,
    isFullscreen: Boolean,
    onEpisodeChange: (Int) -> Unit,
    onFullScreenChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {

    var realPosition by remember(playerState) { mutableLongStateOf(playerState.position) } // sync

    if (playerState.isPlaying) {
        LaunchedEffect(Unit) {
            while (true) {
                realPosition = player.currentPosition
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

    if (isBottomControllerVisible) {
        LaunchedEffect(Unit) {
            delay(5.seconds)
            isBottomControllerVisible = false
            if (isFullscreen)
                isTopControllerVisible = false
        }
    }

    if (player.mediaItemCount != 0 && playerState.bufferedPercentage == 0) {
        LaunchedEffect(Unit) {
            delay(45.seconds)
            Log.d("Video", "视频加载超时，尝试备用地址")
            player.seekToNextMediaItem()
        }
    }

    Box(modifier) {
        if (playerState.isLoading) {
            LoadingDotsVariant(
                Modifier
                    .zIndex(1f)
                    .align(Alignment.Center)
            )
        }
        AndroidView(
            modifier = Modifier.pointerInput(isFullscreen) {
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
                    }
                )
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
            enter = fadeIn(spring(stiffness = 4_000f)),
            exit = fadeOut(spring(stiffness = 1_000f))
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

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = isBottomControllerVisible,
            enter = fadeIn(spring(stiffness = 4_000f)),
            exit = fadeOut(spring(stiffness = 1_000f))
        ) {
            BottomController(
                modifier = Modifier.background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.5f)))
                ),
                isPaused = playerState.isPaused,
                currentEpisode = if (uiState is AnimePlayUiState.Data) uiState.episode.value else 1,
                totalEpisodes = if (uiState is AnimePlayUiState.Data) uiState.anime.latestEpisode else 1,
                currentPosition = realPosition,
                totalDurationMs = playerState.totalDurationMs,
                bufferedPercentage = playerState.bufferedPercentage,
                isFullscreen = isFullscreen,
                onPlay = player::play,
                onPause = player::pause,
                onFullScreen = { onFullScreenChange(true) },
                seekTo = {
                    player.seekTo(it)
                    realPosition = it
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
            tint = basicWhite
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
    seekTo: (Long) -> Unit,
    onEpisodeChange: (Int) -> Unit,
    showEpisodesDrawer: () -> Unit,
) {
    val totalDuration = formatMilliseconds(totalDurationMs)
    val currentPos = formatMilliseconds(currentPosition, totalDuration.length > 5)
    if (isFullscreen) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .displayCutoutPadding()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentPos,
                    color = basicWhite,
                    style = MaterialTheme.typography.labelSmall
                )
                SeekBar(
                    modifier = Modifier.weight(1f),
                    currentPosition = currentPosition,
                    totalDurationMs = totalDurationMs,
                    bufferedPercentage = bufferedPercentage,
                    seekTo = seekTo
                )
                Text(
                    text = totalDuration,
                    color = basicWhite,
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
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    AnimatedPlayPauseButton(
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
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(12.dp, 8.dp),
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
                seekTo = seekTo
            )
            Text(
                text = "$currentPos / $totalDuration",
                color = basicWhite,
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
                modifier = Modifier
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
                modifier = Modifier
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
    seekTo: (Long) -> Unit,
) {
    Box(
        modifier = modifier
            .requiredHeight(24.dp)
            .clipToBounds(),
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
            onValueChange = { seekTo(it.toLong()) },
            valueRange = 0f..totalDurationMs.toFloat(),
            thumb = {
                Icon(
                    modifier = Modifier
                        .offset(1.dp, 5.dp)
                        .blur(2.dp),
                    painter = painterResource(NekoAnimeIcons.Player.thumb),
                    contentDescription = "thumb shadow",
                    tint = basicBlack.copy(0.5f)
                )
                Image(
                    modifier = Modifier.offset(y = 4.dp),
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
        String.format("%d:%02d:%02d", hours, minutes - hours * 60, seconds - minutes * 60)
    } else {
        String.format("%02d:%02d", minutes, seconds - minutes * 60)
    }
}