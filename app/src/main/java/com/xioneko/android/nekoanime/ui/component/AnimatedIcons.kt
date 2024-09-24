package com.xioneko.android.nekoanime.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.neutral01

@Composable
fun AnimatedFollowIcon(
    modifier: Modifier = Modifier,
    isFollowed: Boolean
) {
    val followIcon by rememberLottieComposition(
        LottieCompositionSpec.RawRes(NekoAnimeIcons.Animated.follow)
    )
    val animationProgress by animateFloatAsState(
        targetValue = if (isFollowed) 1f else 0f,
        animationSpec = tween(800, easing = LinearEasing), label = ""
    )

    LottieAnimation(
        modifier = modifier,
        composition = followIcon,
        progress = { if (isFollowed) animationProgress else 0f })
}

@Composable
fun AnimatedRadioButton(
    modifier: Modifier = Modifier,
    selected: Boolean,
    durationMillis: Int = 600,
) {
    val radioButton by rememberLottieComposition(
        LottieCompositionSpec.RawRes(NekoAnimeIcons.Animated.radio)
    )

    val selectProgress by animateFloatAsState(
        targetValue = if (selected) 0.5f else 0f,
        animationSpec = tween(durationMillis, easing = LinearEasing), label = ""
    )

    LottieAnimation(
        modifier = modifier,
        composition = radioButton, progress = { if (selected) selectProgress else 0f }
    )
}

@Composable
fun AnimatedSwitchButton(
    modifier: Modifier = Modifier,
    checked: Boolean?,
) {
    val switchButton by rememberLottieComposition(
        LottieCompositionSpec.RawRes(NekoAnimeIcons.Animated.switch)
    )

    if (checked == null) {
        LottieAnimation(
            modifier = modifier,
            composition = switchButton, progress = { 1f }
        )
    } else {
        val animationProgress by animateFloatAsState(
            targetValue = if (checked) 1f else 0f,
            animationSpec = tween(400, easing = LinearEasing), label = ""
        )

        LottieAnimation(
            modifier = modifier,
            composition = switchButton, progress = { animationProgress }
        )
    }
}

@Composable
fun AnimatedCheckbox(
    modifier: Modifier = Modifier,
    checked: Boolean,
) {
    val checkbox by rememberLottieComposition(
        LottieCompositionSpec.RawRes(NekoAnimeIcons.Animated.checkbox)
    )

    val animationProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(600, easing = LinearEasing), label = ""
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .clipToBounds()
            .background(neutral01)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .border(1.dp, Color.White, CircleShape)
        )
        AnimatedVisibility(
            visible = checked,
            enter = EnterTransition.None,
            exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)),
        ) {
            LottieAnimation(
                modifier = Modifier.fillMaxSize(),
                composition = checkbox, progress = { if (checked) animationProgress else 1f }
            )
        }
    }
}