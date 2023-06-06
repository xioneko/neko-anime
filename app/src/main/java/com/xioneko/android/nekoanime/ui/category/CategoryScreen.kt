package com.xioneko.android.nekoanime.ui.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.xioneko.android.nekoanime.data.ANIME_LIST_PAGE_SIZE
import com.xioneko.android.nekoanime.data.model.Category
import com.xioneko.android.nekoanime.data.model.defaultLabel
import com.xioneko.android.nekoanime.ui.component.LoadingDots
import com.xioneko.android.nekoanime.ui.component.NarrowAnimeCard
import com.xioneko.android.nekoanime.ui.component.SolidTopBar
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicBlack
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.theme.darkPink80
import com.xioneko.android.nekoanime.ui.theme.neutral01
import com.xioneko.android.nekoanime.ui.theme.neutral10
import com.xioneko.android.nekoanime.ui.theme.pink40
import com.xioneko.android.nekoanime.ui.theme.pink90


@Composable
fun CategoryScreen(
    filter: Map<Category, Pair<String, String>>,
    viewModel: CategoryViewModel = hiltViewModel(),
    onAnimeClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    LaunchedEffect(Unit) { viewModel.initFilterState(filter) }

    val lazyGridState = rememberLazyGridState()

    val shouldShowLoadingDots by remember { derivedStateOf { viewModel.fetcherState.loadingPageCount > 0 } }
    val shouldFetchMore by remember(viewModel.fetcherState.hasMore) {
        derivedStateOf {
            with(lazyGridState.layoutInfo) {
                viewModel.fetcherState.hasMore &&
                        (totalItemsCount > 12 &&
                                visibleItemsInfo.last().index >
                                viewModel.fetcherState.page * ANIME_LIST_PAGE_SIZE - 8)
            }
        }
    }
    if (shouldFetchMore) viewModel.fetchAnime()

    Scaffold(
        topBar = {
            SolidTopBar(
                title = "分类",
                onLeftIconClick = onBackClick,
                rightIconId = NekoAnimeIcons.search,
                onRightIconClick = onSearchClick
            )
        },
    ) { padding ->
        FiltersBar(
            modifier = Modifier
                .padding(padding),
            filter = viewModel.filter,
            onFilter = { category, input ->
                with(viewModel) {
                    viewModel.fetcherState.reset()
                    viewModel.filter[category] = input
                    fetchAnime()
                }
            }
        )
        LazyVerticalGrid(
            modifier = Modifier
                .zIndex(-1f)
                .padding(padding)
                .fillMaxSize()
                .background(basicWhite),
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 36.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            state = lazyGridState,
        ) {
            for (animeShell in viewModel.animeList) {
                item(animeShell.id) {
                    NarrowAnimeCard(anime = animeShell, onClick = onAnimeClick)
                }
            }
            if (shouldShowLoadingDots) {
                item(
                    key = "Loading",
                    contentType = "Loading",
                    span = { GridItemSpan(maxLineSpan) },
                    content = { LoadingDots() }
                )
            }
        }
    }
}


@Composable
fun FiltersBar(
    modifier: Modifier = Modifier,
    filter: Map<Category, Pair<String, String>>,
    mainCategories: List<Category> = listOf(Category.Genre, Category.Year, Category.Order),
    onFilter: (Category, Pair<String, String>) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    var drawer: Category? by remember { mutableStateOf(null) }
    val otherCategories = remember { Category.values().toSet() - mainCategories.toSet() }
    val hideDrawer = { drawer = null }

    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(basicWhite)
                .padding(end = 16.dp, bottom = 5.dp)
                .zIndex(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            for (category in mainCategories) {
                CategoryHead(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 24.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            role = Role.DropdownList,
                            onClick = { drawer = if (drawer != category) category else null }
                        ),
                    expand = drawer == category,
                    active = filter[category]!!.second != category.defaultLabel(),
                    category = category,
                    label = filter[category]!!.second,
                )
            }
            Icon(
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.DropdownList,
                        onClick = {
                            drawer =
                                if (drawer !in otherCategories) otherCategories.first() else null
                        }
                    )
                    .padding(start = 24.dp),
                painter = painterResource(NekoAnimeIcons.filter),
                contentDescription = null,
                tint = if (filter.filterKeys { it in otherCategories }
                        .any { (category, pair) -> // Pair<输出值， 显示值>
                            pair.second != category.defaultLabel()
                        })
                    pink40 else basicBlack
            )
        }
        Box {
            androidx.compose.animation.AnimatedVisibility(
                visible = drawer != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(basicBlack.copy(alpha = 0.1f))
                    .pointerInput(Unit) { detectTapGestures { drawer = null } })
            }
            for (category in mainCategories) {
                AnimatedDrawer(drawer == category) {
                    MainFilterDrawer(
                        filter = filter,
                        onFilter = { category, option ->
                            onFilter(category, option)
                            hideDrawer()
                        },
                        drawer = category
                    )
                }
            }
            AnimatedDrawer(visible = drawer == otherCategories.first()) {
                ExtraFilterDrawer(filter = filter, onFilter = { category, option ->
                    onFilter(category, option)
                    hideDrawer()
                })
            }
        }
    }
}

@Composable
private fun CategoryHead(
    modifier: Modifier = Modifier,
    expand: Boolean,
    active: Boolean,
    category: Category,
    label: String
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = if (active) label else category.title,
            color = if (active) pink40 else basicBlack,
            style = MaterialTheme.typography.bodySmall
        )

        val arrowUpDown by rememberLottieComposition(
            LottieCompositionSpec.RawRes(NekoAnimeIcons.Animated.arrowUpDown)
        )
        val animationProgress by animateFloatAsState(
            targetValue = if (expand) 1f else 0f,
            animationSpec = tween(300, easing = LinearEasing)
        )


        val arrowColor = rememberLottieDynamicProperties(
            rememberLottieDynamicProperty(
                property = LottieProperty.COLOR_FILTER,
                value = if (active) SimpleColorFilter(pink40.toArgb())
                else SimpleColorFilter(basicBlack.toArgb()),
                "**"
            )
        )

        LottieAnimation(
            composition = arrowUpDown,
            progress = { animationProgress },
            dynamicProperties = arrowColor
        )
    }
}

@Composable
private fun AnimatedDrawer(
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { -it },
        exit = slideOutVertically(
            animationSpec = spring(
                stiffness = Spring.StiffnessHigh,
                visibilityThreshold = IntOffset.VisibilityThreshold
            ),
            targetOffsetY = { -it }
        )
    ) {
        Surface(
            Modifier
                .fillMaxWidth()
                .background(
                    color = basicWhite,
                    shape = RoundedCornerShape(bottomEnd = 10.dp, bottomStart = 10.dp)
                )
                .padding(
                    top = 5.dp,
                    start = 24.dp,
                    end = 24.dp,
                )
        ) {
            content()
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MainFilterDrawer(
    filter: Map<Category, Pair<String, String>>,
    onFilter: (Category, Pair<String, String>) -> Unit,
    drawer: Category
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        for (option in drawer.options) {
            OptionChip(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .widthIn(min = 56.dp, max = 94.dp)
                    .background(
                        color = if (filter[drawer] == option) pink90 else neutral01,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Checkbox,
                        onClick = { onFilter(drawer, option) }
                    ),
                text = option.second,
                fontColor = if (filter[drawer] == option) darkPink80 else neutral10
            )
        }
    }
}

@Composable
private fun ExtraFilterDrawer(
    filter: Map<Category, Pair<String, String>>,
    categories: List<Category> = listOf(Category.Region, Category.Type, Category.Quarter),
    onFilter: (Category, Pair<String, String>) -> Unit,
) {
    Column {
        for (category in categories) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = category.title, style = MaterialTheme.typography.bodySmall)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (option in category.options) {
                        OptionChip(
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .background(
                                    color = if (filter[category] == option) pink90 else neutral01,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    role = Role.Checkbox,
                                    onClick = { onFilter(category, option) }
                                ),
                            text = option.second,
                            fontColor = if (filter[category] == option) darkPink80 else neutral10
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionChip(
    modifier: Modifier,
    text: String,
    fontColor: Color,
) {
    Box(
        modifier = modifier
            .padding(vertical = 5.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = fontColor,
            style = MaterialTheme.typography.bodySmall
        )
    }
}