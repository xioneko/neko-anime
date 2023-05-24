package com.xioneko.android.nekoanime.ui.schedule

import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.ui.component.LoadingDots
import com.xioneko.android.nekoanime.ui.component.TransparentTopBar
import com.xioneko.android.nekoanime.ui.component.WorkingInProgressDialog
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.theme.darkPink60
import com.xioneko.android.nekoanime.ui.theme.neutral01
import com.xioneko.android.nekoanime.ui.theme.neutral08
import com.xioneko.android.nekoanime.ui.theme.neutral10
import com.xioneko.android.nekoanime.ui.theme.pink10
import com.xioneko.android.nekoanime.ui.theme.pink50
import com.xioneko.android.nekoanime.ui.theme.pink60
import com.xioneko.android.nekoanime.ui.theme.pink70
import com.xioneko.android.nekoanime.ui.theme.pink80
import com.xioneko.android.nekoanime.ui.theme.pink99
import kotlinx.coroutines.launch
import okhttp3.internal.format
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScheduleScreen(
    padding: PaddingValues,
    viewModel: ScheduleViewModel = hiltViewModel(),
    onAnimeClick: (Int) -> Unit
) {
    var shouldShowFilterMenu by remember { mutableStateOf(false) }
    if (shouldShowFilterMenu) {
        // TODO: 可以按条件（如已追番、已完结）过滤番剧
        WorkingInProgressDialog { shouldShowFilterMenu = false }
    }

    val localDate = LocalDate.now()
    val pagerState = rememberPagerState(localDate.dayOfWeek.ordinal)
    val scrollState = rememberScrollState()
    val weeklySchedule by viewModel.weeklySchedule.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .padding(padding)
            .background(pink99),
        topBar = {
            TransparentTopBar(
                title = "时间表",
                iconId = NekoAnimeIcons.filter2,
                onIconClick = { shouldShowFilterMenu = true }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .padding(15.dp, 16.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                val animationScope = rememberCoroutineScope()
                DayOfWeek.values().forEach {
                    val selected = pagerState.currentPage == it.ordinal
                    if (selected) animationScope.launch {
                        scrollState.animateScrollTo(it.ordinal)
                    }
                    DayBox(
                        modifier = Modifier
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
                    pageCount = 7,
                    state = pagerState,
                ) { index ->
                    Box(Modifier.fillMaxSize()) {
                        if (weeklySchedule.isEmpty()) LoadingDots(Modifier.align(Alignment.Center))

                        androidx.compose.animation.AnimatedVisibility(
                            visible = weeklySchedule.isNotEmpty(),
                            enter = fadeIn()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 30.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Spacer(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(topCurtainHeight)
                                )
                                for (animeShell in weeklySchedule[DayOfWeek.of(index + 1)]!!) {
                                    ScheduleItem(
                                        modifier = Modifier
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

@Composable
private fun DayBox(
    modifier: Modifier = Modifier,
    selected: Boolean,
    date: LocalDate,
) {
    val dayBoxSize = DpSize(40.dp, 60.dp)
    val dayBoxShape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .size(dayBoxSize)
            .clip(dayBoxShape)
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
        SideLine(Modifier.height(80.dp))
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
                modifier = Modifier.width(56.dp),
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

private val literalOf: Map<DayOfWeek, String> = mapOf(
    DayOfWeek.MONDAY to "一",
    DayOfWeek.TUESDAY to "二",
    DayOfWeek.WEDNESDAY to "三",
    DayOfWeek.THURSDAY to "四",
    DayOfWeek.FRIDAY to "五",
    DayOfWeek.SATURDAY to "六",
    DayOfWeek.SUNDAY to "日"
)