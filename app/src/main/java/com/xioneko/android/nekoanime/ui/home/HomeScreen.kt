package com.xioneko.android.nekoanime.ui.home

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.ui.component.AnimeGrid
import com.xioneko.android.nekoanime.ui.component.FollowedAnimeCard
import com.xioneko.android.nekoanime.ui.component.NekoAnimeSnackBar
import com.xioneko.android.nekoanime.ui.component.NekoAnimeSnackbarHost
import com.xioneko.android.nekoanime.ui.component.shimmerBrush
import com.xioneko.android.nekoanime.ui.search.SearchScreen
import com.xioneko.android.nekoanime.ui.search.rememberSearchBarState
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeFontFamilies
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeFontFamilies.heiFontFamily
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicBlack
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.theme.brightNeutral05
import com.xioneko.android.nekoanime.ui.theme.brightNeutral06
import com.xioneko.android.nekoanime.ui.theme.neutral05
import com.xioneko.android.nekoanime.ui.theme.pink40
import com.xioneko.android.nekoanime.ui.theme.pink50
import com.xioneko.android.nekoanime.ui.theme.pink95
import com.xioneko.android.nekoanime.ui.util.LoadingState
import com.xioneko.android.nekoanime.ui.util.getAspectRadio
import com.xioneko.android.nekoanime.ui.util.isTablet
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    padding: PaddingValues,
    viewModel: HomeViewModel = hiltViewModel(),
    onCategoryClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onAnimeClick: (Int) -> Unit,
    onFollowedAnimeClick: () -> Unit,
    navigateToCategory: (genre: String) -> Unit,
) {
    val aspectRatio = getAspectRadio()
    val isTablet = isTablet()

    val focusRequester = remember { FocusRequester() }
    val lazyListState = rememberLazyListState()
    val scrollProgress by remember {
        derivedStateOf {
            if (aspectRatio > 1.8 && lazyListState.firstVisibleItemIndex == 0)
                (lazyListState.firstVisibleItemScrollOffset / 300f).coerceAtMost(1f)
            else 1f
        }
    }

    val searchBarState =
        rememberSearchBarState(viewModel.isSearching, focusRequester, scrollProgress)
    val systemUiController = rememberSystemUiController()

    val loadingState by viewModel.loadingState.collectAsStateWithLifecycle()

    var refreshing by rememberSaveable { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        refreshingOffset = 124.dp,
        onRefresh = {
            if (loadingState is LoadingState.LOADING) return@rememberPullRefreshState

            refreshing = true
            viewModel.refresh {
                refreshing = false
            }
        }
    )

    LaunchedEffect(viewModel.isSearching, scrollProgress) {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = viewModel.isSearching || scrollProgress > 0.5f
        )
    }

    val followedAnime by viewModel.followedAnime.collectAsStateWithLifecycle()
    val recentUpdates by viewModel.recentUpdates.collectAsStateWithLifecycle()


    SearchScreen(
        modifier = Modifier
            .zIndex(1f),
        viewModel = hiltViewModel(),
        uiState = searchBarState,
        onEnterExit = { viewModel.isSearching = it },
        onCategoryClick = onCategoryClick,
        onHistoryClick = onHistoryClick,
        onAnimeClick = onAnimeClick
    )

    Box(
        Modifier
            .padding(padding)
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(top = if (aspectRatio > 1.8) 0.dp else 86.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (aspectRatio > 1.8) // 为了更舒服的的 UI 布局，暂时在较小纵横比的屏幕下隐藏轮播图
                item("Carousel") { Carousel(onSlideClick = onAnimeClick) }

            if (followedAnime.isNotEmpty()) {
                item("Followed Anime") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StylizedHead(text = "我的追番")
                            MoreInfo(onFollowedAnimeClick)
                        }
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (anime in followedAnime.reversed()) {
                                item(anime.id) {
                                    FollowedAnimeCard(
                                        modifier = Modifier
                                            .width(
                                                if (isTablet) min(
                                                    240.dp,
                                                    (LocalConfiguration.current.screenWidthDp / 3).dp
                                                ) else 116.dp
                                            ),
                                        anime = anime,
                                        onClick = onAnimeClick
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item("Recent Updates") {
                AnimeGridWithHead(
                    headline = "最近更新",
                    animeList = recentUpdates,
                    useExpandCardStyle = true,
                    onMoreDetails = { navigateToCategory("") },
                    onAnimeClick = onAnimeClick
                )
            }

            viewModel.forYouAnimeStreams.forEachIndexed { index, animeFlow ->
                item(index) {
                    val genreToAnimeList by animeFlow.collectAsStateWithLifecycle()
                    AnimeGridWithHead(
                        headline = genreToAnimeList.first,
                        animeList = genreToAnimeList.second,
                        useExpandCardStyle = false,
                        onMoreDetails = navigateToCategory,
                        onAnimeClick = onAnimeClick
                    )
                }
            }
        }
        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = refreshing,
            state = pullRefreshState,
            contentColor = pink40
        )
        NekoAnimeSnackbarHost(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = loadingState is LoadingState.FAILURE,
            message = { (loadingState as LoadingState.FAILURE).message }
        ) {
            NekoAnimeSnackBar(
                modifier = Modifier.requiredWidth(200.dp),
                snackbarData = it
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Carousel(
    onSlideClick: (Int) -> Unit
) {
    val slides = Slide.entries.toTypedArray()
    val pagerState = rememberPagerState(
        initialPage = Random.nextInt(0, slides.size),
        initialPageOffsetFraction = 0f
    ) { slides.size }

    LaunchedEffect(Unit) {
        while (true) {
            val beforePage = pagerState.currentPage
            delay(5.seconds)
            if (beforePage == pagerState.currentPage)
                pagerState.animateScrollToPage(
                    page = (pagerState.currentPage + 1) % slides.size,
                    pageOffsetFraction = 0f,
                    animationSpec = tween()
                )
        }
    }

    Box {
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)),
            state = pagerState,
            contentPadding = PaddingValues(0.dp),
            pageSize = PageSize.Fill,
            beyondBoundsPageCount = 0,
            pageSpacing = 0.dp,
            verticalAlignment = Alignment.CenterVertically,
        ) { page ->
            val slide = slides[page]
            Box(
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = { onSlideClick(slide.animeId) }
                )
            ) {
                Image(
                    modifier = Modifier.fillMaxWidth(),
                    painter = painterResource(slide.imageId),
                    contentDescription = slide.title,
                    contentScale = ContentScale.Crop,
                )
                Box(
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(0.6f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 12.dp, end = 12.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextWithShadow(
                        text = slide.title,
                        color = basicWhite,
                        fontFamily = NekoAnimeFontFamilies.posterFontFamily,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = slide.description,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = pink95,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 15.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(slides.size) {
                Box(
                    Modifier
                        .size(8.dp)
                        .padding(if (pagerState.currentPage % slides.size == it) 0.dp else 1.dp)
                        .background(
                            color = if (pagerState.currentPage % slides.size == it)
                                basicWhite.copy(0.9f)
                            else basicWhite.copy(0.4f),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun AnimeGridWithHead(
    headline: String?,
    animeList: List<AnimeShell?>,
    useExpandCardStyle: Boolean,
    onMoreDetails: (genre: String) -> Unit,
    onAnimeClick: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (headline == null) {
                Box(
                    modifier = Modifier
                        .size(128.dp, 20.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(shimmerBrush(x = 128.dp, y = 20.dp, brightNeutral06))
                )
                Box(
                    modifier = Modifier
                        .size(64.dp, 16.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(shimmerBrush(x = 128.dp, y = 16.dp, brightNeutral05))
                )
            } else {
                StylizedHead(text = headline)
                MoreInfo { onMoreDetails(headline) }
            }
        }
        AnimeGrid(
            modifier = Modifier.padding(horizontal = 12.dp),
            useExpandCardStyle = useExpandCardStyle,
            animeList = animeList,
            onAnimeClick = onAnimeClick
        )
    }
}

@Composable
private fun MoreInfo(
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Button,
            onClick = onClick
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "更多",
            color = neutral05,
            style = MaterialTheme.typography.bodySmall
        )
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(NekoAnimeIcons.arrowRight),
            contentDescription = null,
            tint = neutral05
        )
    }
}

@Composable
private fun StylizedHead(
    text: String,
) {
    Box(Modifier.width(IntrinsicSize.Max)) {
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(if (text.length < 3) 1f else 0.7f)
                .height(7.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            pink50,
                            pink50.copy(alpha = 0f)
                        )
                    ),
                    shape = CircleShape
                )
        )
        Text(
            modifier = Modifier.padding(bottom = 1.dp),
            text = text,
            fontFamily = heiFontFamily,
            fontWeight = FontWeight.Bold,
            color = basicBlack,
            style = MaterialTheme.typography.titleMedium
        )
    }
}


@Composable
private fun TextWithShadow(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    fontFamily: FontFamily,
    style: TextStyle,
) {
    Box {
        Text(
            modifier = modifier
                .offset(2.dp, 2.dp)
                .alpha(0.5f),
            text = text,
            color = Color.DarkGray,
            fontFamily = fontFamily,
            style = style
        )
        Text(
            modifier = modifier,
            text = text,
            color = color,
            fontFamily = fontFamily,
            style = style
        )
    }
}
