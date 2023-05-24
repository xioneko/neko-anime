package com.xioneko.android.nekoanime.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons

@Composable
fun LoadingDots(modifier: Modifier = Modifier) =
    InfiniteAnimation(modifier, NekoAnimeIcons.Animated.loading)

@Composable
fun LoadingDotsVariant(modifier: Modifier = Modifier) =
    InfiniteAnimation(modifier, NekoAnimeIcons.Animated.videoLoading)

@Composable
private fun InfiniteAnimation(modifier: Modifier, res: Int) {
    val loading by
    rememberLottieComposition(LottieCompositionSpec.RawRes(res))
    val animationProgress
            by animateLottieCompositionAsState(
                composition = loading,
                iterations = Int.MAX_VALUE
            )
    LottieAnimation(modifier = modifier, composition = loading, progress = { animationProgress })
}