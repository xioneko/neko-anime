package com.xioneko.android.nekoanime.ui.player

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.hardware.SensorManager
import android.view.OrientationEventListener
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.ui.component.AnimatedFollowIcon
import com.xioneko.android.nekoanime.ui.component.AnimeGrid
import com.xioneko.android.nekoanime.ui.component.NekoAnimeSnackBar
import com.xioneko.android.nekoanime.ui.component.NekoAnimeSnackbarHost
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicBlack
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.theme.darkPink60
import com.xioneko.android.nekoanime.ui.theme.neutral01
import com.xioneko.android.nekoanime.ui.theme.neutral08
import com.xioneko.android.nekoanime.ui.theme.neutral10
import com.xioneko.android.nekoanime.ui.theme.pink10
import com.xioneko.android.nekoanime.ui.theme.pink30
import com.xioneko.android.nekoanime.ui.theme.pink60
import com.xioneko.android.nekoanime.ui.theme.pink70
import com.xioneko.android.nekoanime.ui.theme.pink97
import com.xioneko.android.nekoanime.ui.util.LoadingState
import com.xioneko.android.nekoanime.ui.util.isOrientationLocked
import com.xioneko.android.nekoanime.ui.util.isTablet
import com.xioneko.android.nekoanime.ui.util.setScreenOrientation
import kotlinx.coroutines.delay

@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun AnimePlayScreen(
    animeId: Int,
    viewModel: AnimePlayViewModel = hiltViewModel(),
    onGenreClick: (String) -> Unit,
    onAnimeClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.loadingUiState(animeId) }
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val systemUiController = rememberSystemUiController()
    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    val enablePortraitFullscreen by viewModel.isEnablePortraitFullscreen.collectAsStateWithLifecycle()
    val onFullscreenChange: (Boolean) -> Unit = remember {
        { enable ->
            isFullscreen = enable
            if (enablePortraitFullscreen) {
                if (!enable && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    context.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            } else {
                context.setScreenOrientation(
                    if (enable) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                )
            }
        }
    }
    LaunchedEffect(configuration.orientation) {
        when (configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                isFullscreen = true
            }

            Configuration.ORIENTATION_PORTRAIT -> {
                if (!enablePortraitFullscreen) isFullscreen = false
            }

            else -> {}
        }
    }
    LaunchedEffect(isFullscreen) {
        systemUiController.run {
            setStatusBarColor(Color.Transparent, false)
            if (isFullscreen) {
                isSystemBarsVisible = false
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                isSystemBarsVisible = true
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    DisposableEffect(Unit) {
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

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFullscreen) {
                    onFullscreenChange(false)
                } else {
                    onBackClick()
                }
            }
        }
        backDispatcher?.addCallback(backCallback)

        val orientationListener =
            object : OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
                override fun onOrientationChanged(angle: Int) {
                    if (context.isOrientationLocked()) return

                    when (configuration.orientation) {
                        Configuration.ORIENTATION_PORTRAIT ->
                            if (angle <= 5 || angle >= 355) {
                                viewModel.unlockOrientation(context)
                            }

                        Configuration.ORIENTATION_LANDSCAPE ->
                            if (angle in 85..95 || angle in 265..275) {
                                viewModel.unlockOrientation(context)
                            }

                        else -> {}
                    }
                }

            }.apply { enable() }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            backCallback.remove()
            orientationListener.disable()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            if (viewModel.uiState is AnimePlayUiState.Data) {
                viewModel.upsertWatchRecord(
                    (viewModel.uiState as AnimePlayUiState.Data).episode.value
                )
            }
        }
    }

    val onEpisodeChange: (Int) -> Unit = remember {
        {
            with(viewModel) {
                with(uiState as AnimePlayUiState.Data) {
                    // 只有获取了 uiState 中 episode 相关信息才可能被调用
                    upsertWatchRecord(episode.value)
                    episode.value = it
                }
                player.update()
            }
        }
    }

    val loadingState by viewModel.loadingState.collectAsStateWithLifecycle()

    val forYouAnimeList by viewModel.forYouAnimeStream.collectAsStateWithLifecycle()

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

    Surface(
        color = basicWhite
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                NekoAnimePlayer(
                    modifier = playerModifier,
                    player = viewModel.player,
                    uiState = viewModel.uiState,
                    playerState = playerState,
                    onEpisodeChange = onEpisodeChange,
                    isFullscreen = isFullscreen,
                    onFullScreenChange = onFullscreenChange,
                    onBack = {
                        if (isFullscreen) onFullscreenChange(false)
                        else onBackClick()
                    },
                )
                Column(Modifier.navigationBarsPadding()) {
                    if (viewModel.uiState is AnimePlayUiState.Loading) AnimePlayBodySkeleton()
                    else {
                        with(viewModel.uiState as AnimePlayUiState.Data) {
                            val isFollowed by viewModel.followed.collectAsStateWithLifecycle()
                            PlayerNeck(
                                anime = anime,
                                isFollowed = isFollowed,
                                onFollowAnime = viewModel::followAnime,
                                onUnfollowAnime = viewModel::unfollowAnime
                            )
                            LazyColumn {
                                item("Anime Detail") {
                                    AnimeDetail(anime, onGenreClick)
                                }
                                item("Episodes List") {
                                    EpisodesList(
                                        currentEpisode = episode.value,
                                        totalEpisodes = anime.latestEpisode,
                                        onEpisodeChange = onEpisodeChange
                                    )
                                }
                                item("For You Anime Grid") {
                                    ForYouAnimeGrid(
                                        animeList = forYouAnimeList,
                                        onAnimeClick = onAnimeClick
                                    )
                                }
                            }
                        }
                    }
                }
            }
            NekoAnimeSnackbarHost(
                modifier = Modifier.align(Alignment.BottomCenter),
                visible = loadingState is LoadingState.FAILURE,
                message = { (loadingState as LoadingState.FAILURE).message }
            ) {
                NekoAnimeSnackBar(
                    modifier = Modifier.requiredWidth(220.dp),
                    snackbarData = it
                )
            }
        }
    }
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
                    text = "更新至第${anime.latestEpisode}话(${anime.status})",
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
    onGenreClick: (String) -> Unit
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
                    text = "${anime.release}上映",
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
                contentDescription = "genre"
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                contentPadding = PaddingValues(end = 5.dp)
            ) {
                for (genre in anime.genres) {
                    item(genre) {
                        Row(
                            modifier = Modifier
                                .background(neutral01, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    role = Role.Button,
                                    onClick = { onGenreClick(genre) }
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
    var maxLines by remember { mutableStateOf(5) }
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
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (ep in 1..totalEpisodes) {
                if (currentEpisode == ep) {
                    item(ep) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(pink70, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                            content = {
                                Text(
                                    text = "$ep",
                                    color = pink10,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        )
                    }
                } else {
                    item(ep) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    role = Role.RadioButton,
                                    onClick = { onEpisodeChange(ep) }
                                )
                                .border(1.dp, pink60, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                            content = {
                                Text(
                                    text = "$ep",
                                    color = neutral10,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ForYouAnimeGrid(
    animeList: List<AnimeShell?>,
    onAnimeClick: (Int) -> Unit,
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
        AnimeGrid(
            modifier = Modifier.padding(horizontal = 12.dp),
            animeList = animeList,
            onAnimeClick = onAnimeClick
        )
    }
}