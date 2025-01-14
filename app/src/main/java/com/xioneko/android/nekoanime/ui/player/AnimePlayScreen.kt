package com.xioneko.android.nekoanime.ui.player

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.hardware.SensorManager
import android.util.Log
import android.view.OrientationEventListener
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.Download.STATE_COMPLETED
import androidx.media3.exoplayer.offline.Download.STATE_DOWNLOADING
import androidx.media3.exoplayer.offline.Download.STATE_QUEUED
import androidx.media3.exoplayer.offline.Download.STATE_STOPPED
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.xioneko.android.nekoanime.data.AnimeDownloadHelper.Companion.STATE_PREPARING
import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.data.model.asAnimeShell
import com.xioneko.android.nekoanime.data.network.danmu.api.DanmuSession
import com.xioneko.android.nekoanime.data.network.danmu.api.DanmukuEvent
import com.xioneko.android.nekoanime.data.network.danmu.dto.DanmakuPresentation
import com.xioneko.android.nekoanime.ui.component.AnimatedFollowIcon
import com.xioneko.android.nekoanime.ui.component.BottomSheet
import com.xioneko.android.nekoanime.ui.component.LazyAnimeGrid
import com.xioneko.android.nekoanime.ui.danmu.DanmakuConfigData
import com.xioneko.android.nekoanime.ui.danmu.rememberDanmakuHostState
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicBlack
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.theme.darkPink60
import com.xioneko.android.nekoanime.ui.theme.neutral01
import com.xioneko.android.nekoanime.ui.theme.neutral08
import com.xioneko.android.nekoanime.ui.theme.neutral10
import com.xioneko.android.nekoanime.ui.theme.pink10
import com.xioneko.android.nekoanime.ui.theme.pink30
import com.xioneko.android.nekoanime.ui.theme.pink50
import com.xioneko.android.nekoanime.ui.theme.pink60
import com.xioneko.android.nekoanime.ui.theme.pink70
import com.xioneko.android.nekoanime.ui.theme.pink97
import com.xioneko.android.nekoanime.ui.util.KEY_DANMAKU_CONFIG_DATA
import com.xioneko.android.nekoanime.ui.util.LoadingState
import com.xioneko.android.nekoanime.ui.util.currentScreenSizeDp
import com.xioneko.android.nekoanime.ui.util.isOrientationLocked
import com.xioneko.android.nekoanime.ui.util.isTablet
import com.xioneko.android.nekoanime.ui.util.rememberPreference
import com.xioneko.android.nekoanime.ui.util.setScreenOrientation
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

@ExperimentalLayoutApi
@ExperimentalMaterial3Api
@OptIn(UnstableApi::class)
@SuppressLint("SourceLockedOrientationActivity", "ReturnFromAwaitPointerEventScope")
@Composable
fun AnimePlayScreen(
    animeId: Int,
    episode: Int? = null,
    episodeName: String?,
    onDownloadedAnimeClick: (AnimeShell) -> Unit,
    onTagClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel =
        hiltViewModel<AnimePlayViewModel, AnimePlayViewModel.AnimePlayViewModelFactory> { factory ->
            factory.create(animeId, episode, episodeName)
        }

    val context = LocalContext.current
    val (_, screenHeight) = currentScreenSizeDp()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val dragGestureState by viewModel.dragGestureState.collectAsStateWithLifecycle()
    val enabledDanmaku by viewModel.enableDanmu.collectAsStateWithLifecycle()
    val danmakuSession by viewModel.danmakuSession.collectAsStateWithLifecycle()

    val enablePortraitFullscreen by viewModel.enablePortraitFullscreen.collectAsStateWithLifecycle()
    val (isFullscreen, setFullscreen) = rememberFullscreenState(enablePortraitFullscreen)

    DisposableAnimePlayEffects(
        viewModel = viewModel,
        enablePortraitFullscreen = enablePortraitFullscreen,
        isFullscreen = isFullscreen.value,
        setFullscreen = setFullscreen,
        onBackClick = onBackClick,
    )

    // 定时更新观看记录
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            if (uiState is AnimePlayUiState.Data) {
                viewModel.upsertWatchRecord(viewModel.episode.value!!)
            }
        }
    }

    val loadingState by viewModel.loadingState.collectAsStateWithLifecycle()

    LaunchedEffect(loadingState) {
        if (loadingState is LoadingState.FAILURE) {
            Toast.makeText(
                context,
                (loadingState as LoadingState.FAILURE).message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    var showBottomSheet by rememberSaveable { mutableStateOf(false) }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = basicWhite
    ) {
        Column {
            NekoAnimePlayer(
                player = viewModel.player,
                uiState = uiState,
                episode = viewModel.episode.value,
                playerState = playerState,
                isFullscreen = isFullscreen.value,
                enableDanmu = enabledDanmaku,
                dragGestureState = dragGestureState,
                onFullScreenChange = setFullscreen,
                onEpisodeChange = viewModel::onEpisodeChange,
                onDragGesture = { viewModel.onDragGesture(context, it) },
                onBack = {
                    if (isFullscreen.value) setFullscreen(false)
                    else onBackClick()
                },
                onStartDownloadDrawerOpen = {
                    if (isFullscreen.value) setFullscreen(false)
                    if (uiState is AnimePlayUiState.Data) {
                        showBottomSheet = true
                    }
                },
                onDanmakuClick = { viewModel.setEnableDanmuku(it) },
                danmuSession = danmakuSession
            )

            Box(
                Modifier
                    .navigationBarsPadding()
                    .pointerInput(dragGestureState !is DragGestureState.None) { // 避免多点触控
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent(PointerEventPass.Initial).changes.forEach { change ->
                                    if (change.pressed
                                        && dragGestureState !is DragGestureState.None
                                    )
                                        change.consume()
                                }
                            }
                        }
                    }) {
                when (uiState) {
                    AnimePlayUiState.Loading -> {
                        AnimePlayBodySkeleton()
                    }

                    is AnimePlayUiState.Data -> {
                        val forYouAnimeList by viewModel.forYouAnimeStream.collectAsStateWithLifecycle()

                        with(uiState as AnimePlayUiState.Data) {
                            Column {
                                val isFollowed by viewModel.followedFlow.collectAsStateWithLifecycle()
                                PlayerNeck(
                                    anime = anime,
                                    isFollowed = isFollowed,
                                    onFollowAnime = viewModel::followAnime,
                                    onUnfollowAnime = viewModel::unfollowAnime
                                )
                                LazyColumn {
                                    item("Anime Detail") {
                                        AnimeDetail(anime, onTagClick)
                                    }
                                    item("Episodes List") {
                                        EpisodesList(
                                            currentEpisode = viewModel.episode.value!!,
                                            totalEpisodes = anime.latestEpisode,
                                            onEpisodeChange = viewModel::onEpisodeChange
                                        )
                                    }
                                    item("For You Anime Grid") {
                                        ForYouAnimeGrid(
                                            animeList = forYouAnimeList,
                                            onAnimeClick = { id, epId, name ->
                                                viewModel.loadingUiState(id)
                                            }
                                        )
                                    }
                                }
                            }
                            if (showBottomSheet && !isFullscreen.value) {
                                val downloadState by viewModel.downloadsState.collectAsStateWithLifecycle()

                                OfflineCacheBottomSheet(
                                    modifier = Modifier.heightIn(
                                        min = (screenHeight / 4).dp,
                                        max = (screenHeight / 2).dp
                                    ),
                                    totalEpisodes = anime.latestEpisode,
                                    downloadsState = downloadState,
                                    onDismiss = { showBottomSheet = false },
                                    onOfflineCache = { viewModel.onOfflineCache(context, it) },
                                    onDownloadedAnimeClick = { onDownloadedAnimeClick(anime.asAnimeShell()) }
                                )
                            }

                        }
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun DisposableAnimePlayEffects(
    viewModel: AnimePlayViewModel,
    enablePortraitFullscreen: Boolean?,
    isFullscreen: Boolean,
    setFullscreen: (Boolean) -> Unit,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    DisposableEffect(Unit) {
        // 根据播放页的状态自动暂停播放器
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.isPausedBeforeLeave = !viewModel.player.playWhenReady
                    viewModel.player.pause()
                }

                Lifecycle.Event.ON_RESUME -> {
                    if (!viewModel.isPausedBeforeLeave) viewModel.player.play()
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    DisposableEffect(configuration.orientation, enablePortraitFullscreen) {
        // 通过 context.setOrientation 锁定设备方向后，若设备的实际朝向就位，则恢复自动旋转
        val orientationListener =
            object : OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
                @OptIn(UnstableApi::class)
                override fun onOrientationChanged(angle: Int) {
                    if (context.isOrientationLocked() || enablePortraitFullscreen != false) return

                    with(configuration) {
                        Log.d("Player", "angle: $angle orientation: $orientation")
                        when (orientation) {
                            Configuration.ORIENTATION_PORTRAIT ->
                                if (angle <= 5 || angle >= 355) {
                                    viewModel.unlockOrientation(context, orientation)
                                }

                            Configuration.ORIENTATION_LANDSCAPE ->
                                if (angle in 85..95 || angle in 265..275) {
                                    viewModel.unlockOrientation(context, orientation)
                                }

                            else -> {}
                        }
                    }
                }

            }
        orientationListener.enable()

        onDispose {
            orientationListener.disable()
        }
    }

    DisposableEffect(isFullscreen) {
        // 返回键处理
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("Player", "BackPressed, isFullscreen $isFullscreen")
                if (isFullscreen) {
                    setFullscreen(false)
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
}

@Composable
fun rememberFullscreenState(
    enablePortraitFullscreen: Boolean?
): Pair<State<Boolean>, (Boolean) -> Unit> {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val systemUiController = rememberSystemUiController()
    val isFullscreen = remember { mutableStateOf(false) }
    val setFullscreen = remember(enablePortraitFullscreen) {
        callback@{ enableFullscreen: Boolean ->
            if (enablePortraitFullscreen == true) {
                isFullscreen.value = enableFullscreen
                return@callback
            }
            Log.d("Orientation", "setFullscreen: $enableFullscreen")
            // 全屏状态改变时，切换设备方向
            if (enableFullscreen) {
                context.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
            } else {
                context.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            }
        }
    }

    // 屏幕方向变化时，更新 isFullscreen 状态
    LaunchedEffect(configuration.orientation, enablePortraitFullscreen) {
        Log.d("Orientation", "Orientation: ${configuration.orientation}")
        when (configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                isFullscreen.value = true
            }

            Configuration.ORIENTATION_PORTRAIT -> {
                isFullscreen.value = false
            }

            else -> {}
        }
    }

    LaunchedEffect(isFullscreen.value) {
        // 全屏和非全屏下的系统状态栏外观变化
        systemUiController.run {
            setStatusBarColor(Color.Transparent, false)
            if (isFullscreen.value) {
                isSystemBarsVisible = false
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                isSystemBarsVisible = true
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        }
    }

    return isFullscreen to setFullscreen
}


@Composable
private fun PlayerNeck(
    anime: Anime,
    isFollowed: Boolean,
    onFollowAnime: (Anime) -> Unit,
    onUnfollowAnime: (Anime) -> Unit,
) {
    val isTablet = isTablet()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                ambientColor = pink10.copy(0.6f),
                spotColor = pink10.copy(0.6f)
            )
            .background(pink97),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp, 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = anime.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = basicBlack,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = anime.status,
                    color = darkPink60,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Box(modifier = Modifier
                .width(if (isTablet) 80.dp else 72.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Switch,
                    onClick = {
                        if (isFollowed) onUnfollowAnime(anime)
                        else onFollowAnime(anime)
                    }
                ),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    AnimatedFollowIcon(
                        modifier = Modifier.scale(1.6f),
                        isFollowed = isFollowed
                    )
                    Text(
                        text = if (isFollowed) "已追番" else "追番 ",
                        color = pink30,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimeDetail(
    anime: Anime,
    onTagClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(NekoAnimeIcons.date),
                    contentDescription = "release"
                )
                Text(
                    text = "${anime.year}年上映",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier.width(120.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(NekoAnimeIcons.tv),
                    contentDescription = "type"
                )
                Text(
                    text = anime.type,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(NekoAnimeIcons.tag),
                contentDescription = "tags"
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                contentPadding = PaddingValues(end = 5.dp)
            ) {
                for (genre in anime.tags) {
                    item(genre) {
                        Row(
                            modifier = Modifier
                                .background(neutral01, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    role = Role.Button,
                                    onClick = { onTagClick(genre) }
                                )
                                .padding(8.dp, 5.dp, 4.dp, 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = genre,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Icon(
                                modifier = Modifier.size(12.dp),
                                painter = painterResource(NekoAnimeIcons.arrowRight),
                                contentDescription = "more"
                            )
                        }
                    }
                }
            }

        }
        AnimeDescription(anime.description)
    }
}

@Composable
private fun AnimeDescription(
    description: String
) {
    var expandable by remember { mutableStateOf(false) }
    var maxLines by remember { mutableIntStateOf(5) }
    var expanded by remember { mutableStateOf(false) }

    Box(Modifier.padding(horizontal = 12.dp)) {
        Text(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = {
                        if (expandable) {
                            maxLines = 5
                            expanded = false
                        }
                    }
                ),
            text = buildAnnotatedString {
                append(description)
                if (expandable && expanded) {
                    pushStyle(SpanStyle(color = neutral10))
                    append(" 收起")
                }
                toAnnotatedString()
            },
            maxLines = maxLines,
            onTextLayout = { textLayoutResult ->
                if (textLayoutResult.hasVisualOverflow) expandable = true
            },
            color = neutral08,
            style = MaterialTheme.typography.labelSmall,
        )
        if (expandable && !expanded) {
            Text(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(72.dp)
                    .background(
                        Brush.horizontalGradient(
                            0.0f to Color.Transparent,
                            0.3f to basicWhite,
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = {
                            maxLines = Int.MAX_VALUE
                            expanded = true
                        }
                    ),
                text = buildAnnotatedString {
                    pushStyle(SpanStyle(color = neutral10))
                    append("      展开")
                    toAnnotatedString()
                },
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun EpisodesList(
    currentEpisode: Int,
    totalEpisodes: Int,
    onEpisodeChange: (Int) -> Unit,
) {
    val (width, _) = currentScreenSizeDp()
    val lazyListState = rememberLazyListState()
    LaunchedEffect(currentEpisode) {
        lazyListState.animateScrollToItem(max(0, currentEpisode - width / (50 + 10) / 2))
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp),
            text = "选集",
            color = basicBlack,
            style = MaterialTheme.typography.titleSmall
        )
        LazyRow(
            state = lazyListState,
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (ep in 1..totalEpisodes) {
                item(ep) {
                    EpisodeBox(
                        selected = ep == currentEpisode,
                        episode = ep,
                        onSelect = { if (ep != currentEpisode) onEpisodeChange(ep) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EpisodeBox(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onSelect: () -> Unit = {},
    episode: Int,
) {
    val styleModifier = if (selected) {
        Modifier.background(pink70, RoundedCornerShape(12.dp))
    } else {
        Modifier.border(1.dp, pink60, RoundedCornerShape(12.dp))
    }
    Box(
        modifier = modifier
            .size(50.dp)
            .then(styleModifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onSelect
            ),
        contentAlignment = Alignment.Center,
        content = {
            Text(
                text = "$episode",
                color = if (selected) pink10 else neutral10,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    )
}

@Composable
private fun ForYouAnimeGrid(
    animeList: List<AnimeShell?>,
    onAnimeClick: (Int, Int?, String?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            modifier = Modifier.padding(start = 12.dp),
            text = "猜你喜欢",
            color = basicBlack,
            style = MaterialTheme.typography.titleSmall,
        )
        LazyAnimeGrid(
            modifier = Modifier.requiredHeightIn(max = 960.dp),
            horizontalPadding = 12.dp,
            animeList = animeList,
            onAnimeClick = onAnimeClick
        )
    }
}

@Composable
private fun OfflineCacheBottomSheet(
    modifier: Modifier = Modifier,
    totalEpisodes: Int,
    downloadsState: Map<Int, Int>?,
    onDismiss: () -> Unit,
    onOfflineCache: (Int) -> Unit,
    onDownloadedAnimeClick: () -> Unit,
) {
    BottomSheet(
        onDismiss = onDismiss,
        skipPartiallyExpanded = true,
    ) { requestDismiss ->
        Column(modifier.padding(bottom = 15.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp)
                    .offset(y = (-12).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "离线缓存",
                    color = basicBlack,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Row(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            requestDismiss {
                                onDownloadedAnimeClick()
                            }
                        }
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "查看",
                        color = neutral10,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Icon(
                        painter = painterResource(NekoAnimeIcons.arrowRight),
                        contentDescription = "查看"
                    )
                }
            }
            HorizontalDivider(color = neutral01)
            OfflineCacheEpisodesGrid(
                modifier = Modifier.navigationBarsPadding(),
                totalEpisodes = totalEpisodes,
                downloadsStatus = downloadsState,
                onOfflineCache = onOfflineCache
            )
        }
    }
}


@OptIn(UnstableApi::class)
@Composable
private fun OfflineCacheEpisodesGrid(
    modifier: Modifier = Modifier,
    totalEpisodes: Int,
    downloadsStatus: Map<Int, Int>?,
    onOfflineCache: (Int) -> Unit,
) {
    val completedStates = setOf(STATE_COMPLETED)
    val notCompletedStates = setOf(
        STATE_PREPARING,
        STATE_QUEUED,
        STATE_STOPPED,
        STATE_DOWNLOADING
    )

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(50.dp),
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(totalEpisodes) { index ->
            val episode = index + 1
            val status = downloadsStatus?.get(episode)
            var isCompleted by remember(status) {
                mutableStateOf(
                    when (status) {
                        in completedStates -> true
                        in notCompletedStates -> false
                        else -> null
                    }
                )
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box {
                    EpisodeBox(
                        selected = false,
                        episode = episode,
                        onSelect = {
                            if (isCompleted == null) {
                                onOfflineCache(episode)
                                isCompleted = false // 乐观更新
                            }
                        }
                    )
                    if (isCompleted != null) {
                        Box(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .offset(5.dp, 5.dp)
                                .size(16.dp)
                                .background(
                                    if (isCompleted!!) pink30 else pink50,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted!!) {
                                Icon(
                                    modifier = Modifier.size(12.dp),
                                    painter = painterResource(
                                        NekoAnimeIcons.play
                                    ),
                                    tint = Color.White,
                                    contentDescription = "Download Complete"
                                )
                            } else {
                                Icon(
                                    modifier = Modifier.size(12.dp),
                                    painter = painterResource(
                                        NekoAnimeIcons.arrowDown
                                    ),
                                    tint = Color.White,
                                    contentDescription = "Downloading"
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
fun DanmakuHost(
    playerState: NekoAnimePlayerState,
    session: DanmuSession?,
    enabled: Boolean,
    player: ExoPlayer
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
        com.xioneko.android.nekoanime.ui.danmu.DanmakuHost(state = danmakuHostState)
    }

    LaunchedEffect(playerState.isPlaying) {
        if (playerState.isPlaying) {
            danmakuHostState.play()
        } else {
            danmakuHostState.pause()
        }
    }

    val isPlayingFlow = remember { snapshotFlow { player.isPlaying } }
    LaunchedEffect(session) {
        danmakuHostState.clearPresentDanmaku()
        session?.at(
            curTimeMillis = { player.currentPosition.milliseconds },
            isPlayingFlow = isPlayingFlow,
        )?.collect { danmakuEvent ->
            when (danmakuEvent) {
                is DanmukuEvent.Add -> {
                    danmakuHostState.trySend(
                        DanmakuPresentation(
                            danmakuEvent.danmu,
                            false
                        )
                    )
                }
                // 快进/快退
                is DanmukuEvent.Repopulate -> danmakuHostState.repopulate()
            }
        }
    }
}
