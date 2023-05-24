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