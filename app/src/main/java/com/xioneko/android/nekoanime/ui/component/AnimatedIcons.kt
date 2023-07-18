package com.xioneko.android.nekoanime.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons

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
        animationSpec = tween(800, easing = LinearEasing)
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
        animationSpec = tween(durationMillis, easing = LinearEasing)
    )

    LottieAnimation(
        modifier = modifier,
        composition = radioButton, progress = { if (selected) selectProgress else 0f}
    )
}