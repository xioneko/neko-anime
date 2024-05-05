package com.xioneko.android.nekoanime.ui.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.ui.component.AnimatedRadioButton
import com.xioneko.android.nekoanime.ui.component.LoadingDots
import com.xioneko.android.nekoanime.ui.component.NekoAnimeSnackBar
import com.xioneko.android.nekoanime.ui.component.NekoAnimeSnackbarHost
import com.xioneko.android.nekoanime.ui.component.TransparentTopBar
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicBlack
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.theme.darkPink60
import com.xioneko.android.nekoanime.ui.theme.neutral01
import com.xioneko.android.nekoanime.ui.theme.neutral08
import com.xioneko.android.nekoanime.ui.theme.neutral10
import com.xioneko.android.nekoanime.ui.theme.pink10
import com.xioneko.android.nekoanime.ui.theme.pink40
import com.xioneko.android.nekoanime.ui.theme.pink50
import com.xioneko.android.nekoanime.ui.theme.pink60
import com.xioneko.android.nekoanime.ui.theme.pink70
import com.xioneko.android.nekoanime.ui.theme.pink80
import com.xioneko.android.nekoanime.ui.theme.pink99
import com.xioneko.android.nekoanime.ui.util.LoadingState
import com.xioneko.android.nekoanime.ui.util.getAspectRadio
import com.xioneko.android.nekoanime.ui.util.isTablet
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import okhttp3.internal.format
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class, FlowPreview::class)
@Composable
fun ScheduleScreen(
    padding: PaddingValues,
    viewModel: ScheduleViewModel = hiltViewModel(),
    onAnimeClick: (Int) -> Unit
) {
    val isTablet = isTablet()
    val aspectRadio = getAspectRadio()

    val localDate = LocalDate.now()
    val pagerState = rememberPagerState(localDate.dayOfWeek.ordinal, 0f) { 7 }
    val scrollState = rememberScrollState()
    val weeklySchedule by viewModel.weeklySchedule.collectAsStateWithLifecycle()
    val followedAnimeIds by viewModel.followedAnimeIds.collectAsStateWithLifecycle()
    val loadingState by viewModel.loadingState.collectAsStateWithLifecycle()

    var shouldShowFilterMenu by remember { mutableStateOf(false) }


    Scaffold(
        modifier = Modifier
            .padding(padding)
            .pointerInput(Unit) {
                detectTapGestures(onPress = { shouldShowFilterMenu = false })
            },
        topBar = {
            TransparentTopBar(
                title = "时间表",
                iconId = NekoAnimeIcons.filter2,
                iconTint = if (viewModel.filterType == ScheduleFilterType.ALL) basicBlack else pink40,
                onIconClick = { shouldShowFilterMenu = !shouldShowFilterMenu }
            )
        },
        snackbarHost = {
            NekoAnimeSnackbarHost(
                visible = loadingState is LoadingState.FAILURE,
                message = { (loadingState as LoadingState.FAILURE).message }
            ) {
                NekoAnimeSnackBar(
                    modifier = Modifier.requiredWidth(200.dp),
                    snackbarData = it
                )
            }
        },
        containerColor = pink99,
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        Box(Modifier.padding(padding)) {
            AnimatedVisibility(
                modifier = Modifier
                    .zIndex(1f)
                    .align(Alignment.TopEnd)
                    .offset(x = (-20).dp),
                visible = shouldShowFilterMenu,
                enter = scaleIn(initialScale = 0.8f) + fadeIn(),
                exit = scaleOut(targetScale = 0.8f) + fadeOut()
            ) {
                FilterMenu(
                    currentFilterType = viewModel.filterType,
                    onFilterChange = {
                        viewModel.filterType = it
                        shouldShowFilterMenu = false
                    }
                )
            }

            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(15.dp, 16.dp),
                    horizontalArrangement = if (aspectRadio < 0.55 || isTablet) Arrangement.SpaceEvenly
                    else Arrangement.spacedBy(15.dp)
                ) {
                    val animationScope = rememberCoroutineScope()
                    DayOfWeek.entries.forEach {
                        val selected = pagerState.currentPage == it.ordinal
                        if (selected) animationScope.launch {
                            scrollState.animateScrollTo(it.ordinal)
                        }
                        DayBox(
                            modifier = Modifier
                                .size(if (isTablet) DpSize(48.dp, 72.dp) else DpSize(40.dp, 60.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    role = Role.RadioButton,
                                    onClick = {
                                        animationScope.launch {
                                            pagerState.animateScrollToPage(it.ordinal)
                                        }
                                    }
                                ),
                            date = localDate.with(it),
                            selected = selected
                        )
                    }
                }

                Box(Modifier.fillMaxSize()) {
                    val topCurtainHeight = 16.dp
                    Box(
                        Modifier
                            .zIndex(1f)
                            .fillMaxWidth()
                            .height(topCurtainHeight)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        pink99,
                                        pink99.copy(0f)
                                    )
                                )
                            )
                    )

                    HorizontalPager(
                        modifier = Modifier.fillMaxSize(),
                        state = pagerState,
                    ) { index ->
                        val lazyListState = remember(pagerState.currentPage) { LazyListState() }
                        if (shouldShowFilterMenu && pagerState.currentPage == index) {
                            LaunchedEffect(Unit) {
                                var debounce = true
                                snapshotFlow { lazyListState.firstVisibleItemScrollOffset }
                                    .conflate()
                                    .transform {
                                        if (debounce) delay(500).also { debounce = false }
                                        else emit("trigger")
                                    }
                                    .take(1)
                                    .onCompletion { shouldShowFilterMenu = false }
                                    .collect()
                            }
                        }

                        Box(Modifier.fillMaxSize()) {
                            if (weeklySchedule.isEmpty()) LoadingDots(Modifier.align(Alignment.Center))

                            androidx.compose.animation.AnimatedVisibility(
                                visible = weeklySchedule.isNotEmpty(),
                                enter = fadeIn()
                            ) {
                                LazyColumn(
                                    modifier = Modifier.padding(horizontal = 30.dp),
                                    state = lazyListState
                                ) {
                                    item("Spacer") {
                                        Spacer(
                                            Modifier
                                                .fillMaxWidth()
                                                .height(topCurtainHeight)
                                        )
                                    }
                                    for (animeShell in weeklySchedule[DayOfWeek.of(index + 1)]!!) {
                                        when (viewModel.filterType) {
                                            ScheduleFilterType.FOLLOWED -> if (animeShell.id !in followedAnimeIds) continue
                                            ScheduleFilterType.SERIALIZING -> if (animeShell.status != "连载中") continue
                                            else -> {}
                                        }
                                        item(animeShell.id) {
                                            ScheduleItem(
                                                modifier = Modifier
                                                    .height(if (isTablet) 88.dp else 80.dp)
                                                    .fillMaxWidth()
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null,
                                                        role = Role.Button,
                                                        onClick = { onAnimeClick(animeShell.id) }
                                                    ),
                                                animeShell = animeShell,
                                                onAnimeClick = onAnimeClick
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun DayBox(
    modifier: Modifier = Modifier,
    selected: Boolean,
    date: LocalDate,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) pink70 else neutral01),
        contentAlignment = Alignment.Center
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = format("%02d", date.dayOfMonth),
                color = if (selected) darkPink60 else neutral08,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = literalOf[date.dayOfWeek]!!,
                color = if (selected) pink10 else neutral08,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ScheduleItem(
    modifier: Modifier = Modifier,
    animeShell: AnimeShell,
    onAnimeClick: (Int) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SideLine()
        ItemCard(
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = { onAnimeClick(animeShell.id) }
                ),
            animeShell = animeShell
        )
    }
}

@Composable
private fun SideLine(
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(1.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(pink80.copy(0.8f), pink50, pink80.copy(0.8f))
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(5.dp)
                .background(color = pink50, shape = CircleShape)
        )
    }
}

@Composable
private fun ItemCard(
    modifier: Modifier = Modifier,
    animeShell: AnimeShell
) {
    val isTablet = isTablet()
    Surface(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = MaterialTheme.shapes.small,
                ambientColor = pink50.copy(0.6f),
                spotColor = pink50.copy(0.6f)
            )
            .clip(MaterialTheme.shapes.small),
        color = basicWhite,
    ) {
        Row(
            modifier = Modifier.padding(6.dp, 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.width(if (isTablet) 64.dp else 56.dp),
                text = "第${animeShell.latestEpisode}话",
                textAlign = TextAlign.Center,
                color = darkPink60,
                style = MaterialTheme.typography.labelSmall
            )
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(1.dp, 36.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(pink60.copy(0f), pink60, pink60.copy(0f))
                        )
                    )
            )
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = animeShell.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = animeShell.status,
                    color = neutral10,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun FilterMenu(
    modifier: Modifier = Modifier,
    currentFilterType: ScheduleFilterType,
    onFilterChange: (ScheduleFilterType) -> Unit,
) {
    val isTablet = isTablet()

    Surface(
        modifier = modifier
            .zIndex(1f)
            .width(if (isTablet) 124.dp else 110.dp)
            .border(
                width = Dp.Hairline,
                brush = Brush.horizontalGradient(
                    0f to Color.Gray.copy(0.1f),
                    1f to Color.Gray.copy(0.3f)
                ),
                shape = RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        color = basicWhite,
    ) {
        Column(
            modifier = Modifier.padding(10.dp, 5.dp)
        ) {
            ScheduleFilterType.entries.forEach {
                RadioWithLabel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isTablet) 36.dp else 28.dp),
                    label = it.label,
                    selected = currentFilterType == it,
                    onSelect = { onFilterChange(it) },
                )
            }
        }
    }
}

@Composable
private fun RadioWithLabel(
    modifier: Modifier,
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.RadioButton,
            onClick = onSelect
        ),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedRadioButton(
            modifier = Modifier.size(16.dp),
            selected = selected,
            durationMillis = 100
        )
        Text(
            text = label,
            color = if (selected) pink40 else basicBlack,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private val literalOf: Map<DayOfWeek, String> = mapOf(
    DayOfWeek.MONDAY to "一",
    DayOfWeek.TUESDAY to "二",
    DayOfWeek.WEDNESDAY to "三",
    DayOfWeek.THURSDAY to "四",
    DayOfWeek.FRIDAY to "五",
    DayOfWeek.SATURDAY to "六",
    DayOfWeek.SUNDAY to "日"
)