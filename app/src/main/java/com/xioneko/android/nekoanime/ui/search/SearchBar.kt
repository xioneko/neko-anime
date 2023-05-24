package com.xioneko.android.nekoanime.ui.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.input.ImeAction
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicBlack
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.theme.darkPink80
import com.xioneko.android.nekoanime.ui.theme.neutral02
import com.xioneko.android.nekoanime.ui.theme.neutral03
import com.xioneko.android.nekoanime.ui.theme.pink50
import com.xioneko.android.nekoanime.ui.theme.pink95


@Composable
fun NekoAnimeSearchBar(
    modifier: Modifier,
    text: String,
    searching: Boolean,
    searchBarState: SearchBarState,
    leftIconId: Int = NekoAnimeIcons.history,
    rightIconId: Int = NekoAnimeIcons.category,
    onLeftIconClick: () -> Unit = {},
    onRightIconClick: () -> Unit = {},
    onFocusChange: (Boolean) -> Unit,
    onInputChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    val interactionResource = remember { MutableInteractionSource() }
    val focusRequester = remember { FocusRequester() }

    // 若第一次进入 Composition 时, Searching 的初始状态已为 true，则立即使 input box 获得焦点
    LaunchedEffect(Unit) { if (searching) focusRequester.requestFocus() }

    Surface(modifier = modifier, color = searchBarState.surfaceColor) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(15.dp, 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            LeftAnimatedIcon(
                isActive = searching,
                iconId = leftIconId,
                iconColor = searchBarState.iconColor,
                interactionResource = interactionResource,
                onExitSearching = { onFocusChange(false); },
                onIconClick = onLeftIconClick
            )
            SearchBox(
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { if (it.hasFocus) onFocusChange(true) },
                text = text,
                color = searchBarState.searchBoxColor,
                contentColor = searchBarState.boxContentColor,
                focusRequester = focusRequester,
                onInputChange = { onInputChange(it) },
                onSearch = onSearch
            )
            RightAnimatedIcon(
                isActive = searching,
                iconId = rightIconId,
                iconColor = searchBarState.iconColor,
                interactionResource = interactionResource,
                onIconClick = onRightIconClick,
                onSearch = onSearch,
            )

        }
    }
}

@Composable
private fun SearchBox(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    contentColor: Color,
    focusRequester: FocusRequester,
    onInputChange: (String) -> Unit,
    onSearch: () -> Unit,
) {

    val textEntered by remember(text) { derivedStateOf { text.isNotEmpty() } }
    BasicTextField(
        modifier = modifier.focusRequester(focusRequester),
        value = text,
        onValueChange = { onInputChange(it) },
        textStyle = MaterialTheme.typography.bodyMedium,
        singleLine = true,
        cursorBrush = SolidColor(pink50),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    ) { innerTextField ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(color)
                .height(30.dp)
                .padding(start = 4.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(NekoAnimeIcons.search),
                contentDescription = null,
                tint = contentColor
            )
            Box(Modifier.weight(1f)) { innerTextField() }
            ClearIcon(
                modifier = Modifier
                    .size(15.dp),
                visible = textEntered,
                interactionResource = remember { MutableInteractionSource() },
                onClearText = {
                    onInputChange("")
                    focusRequester.requestFocus()
                }
            )
        }
    }
}

@Composable
private fun ClearIcon(
    modifier: Modifier = Modifier,
    visible: Boolean,
    interactionResource: MutableInteractionSource,
    onClearText: () -> Unit
) {
    if (visible) {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(neutral02)
                .clickable(
                    interactionSource = interactionResource,
                    indication = null,
                    role = Role.Button,
                    onClick = onClearText
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.fillMaxSize(0.75f),
                painter = painterResource(NekoAnimeIcons.close),
                contentDescription = null,
                tint = basicWhite
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun LeftAnimatedIcon(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    iconId: Int,
    iconColor: Color,
    interactionResource: MutableInteractionSource,
    onExitSearching: () -> Unit,
    onIconClick: () -> Unit,
) {
    AnimatedContent(
        targetState = isActive,
        transitionSpec = {
            fadeIn() + slideInHorizontally() with
                    fadeOut() + slideOutHorizontally() + scaleOut(
                transformOrigin = TransformOrigin(0f, .5f)
            )
        },
        contentAlignment = Alignment.Center
    ) { activated ->
        if (activated) {
            // back icon
            Icon(
                modifier = modifier.clickable(
                    interactionSource = interactionResource,
                    indication = null,
                    role = Role.Button,
                    onClick = onExitSearching
                ),
                painter = painterResource(NekoAnimeIcons.arrowLeft),
                contentDescription = null,
                tint = iconColor
            )
        } else {
            // action icon
            Icon(
                modifier = modifier.clickable(
                    interactionSource = interactionResource,
                    indication = null,
                    role = Role.Button,
                    onClick = onIconClick
                ),
                painter = painterResource(iconId),
                contentDescription = null,
                tint = iconColor
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun RightAnimatedIcon(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    iconId: Int,
    iconColor: Color,
    interactionResource: MutableInteractionSource,
    onSearch: () -> Unit,
    onIconClick: () -> Unit,
) {
    AnimatedContent(
        targetState = isActive,
        transitionSpec = {
            fadeIn() + slideInHorizontally { it / 2 } with
                    fadeOut() + slideOutHorizontally { it / 2 } + scaleOut(
                transformOrigin = TransformOrigin(1f, .5f)
            )
        },
        contentAlignment = Alignment.Center
    ) { activated ->
        if (activated) {
            // search icon
            Text(
                modifier = modifier.clickable(
                    interactionSource = interactionResource,
                    indication = null,
                    role = Role.Button,
                    onClick = onSearch
                ),
                text = "搜索",
                color = darkPink80,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            // action icon
            Icon(
                modifier = modifier.clickable(
                    interactionSource = interactionResource,
                    indication = null,
                    role = Role.Button,
                    onClick = onIconClick
                ),
                painter = painterResource(iconId),
                contentDescription = null,
                tint = iconColor
            )
        }
    }
}

@Composable
fun rememberSearchBarState(
    searching: Boolean,
    scrollProgress: Float
): SearchBarState {
    return remember(scrollProgress, searching) { SearchBarState(searching, scrollProgress) }
}

@Stable
class SearchBarState(
    var searching: Boolean,
    private val scrollProgress: Float,
) {
    val surfaceColor
        get() =
            if (searching) basicWhite
            else lerp(
                start = basicWhite.copy(alpha = 0f),
                stop = basicWhite,
                fraction = scrollProgress
            )

    val iconColor
        get() =
            if (searching) basicBlack
            else lerp(
                start = basicWhite,
                stop = basicBlack,
                fraction = scrollProgress
            )
    val searchBoxColor
        get() =
            if (searching) pink95
            else lerp(
                start = pink95.copy(alpha = 0.15f),
                stop = pink95,
                fraction = scrollProgress
            )
    val boxContentColor
        get() =
            if (searching) neutral03
            else
                lerp(
                    start = Color.White.copy(alpha = 0.30f),
                    stop = neutral03,
                    fraction = scrollProgress
                )
}