package com.xioneko.android.nekoanime.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.xioneko.android.nekoanime.navigation.NekoAnimeScreen
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.theme.darkPink30
import com.xioneko.android.nekoanime.ui.theme.pink50

@Composable
fun NekoAnimeBottomAppBar(
    modifier: Modifier = Modifier,
    currentDestination: NavDestination?,
    onNavigateTo: (NekoAnimeScreen) -> Unit,
) {
    Surface(
        modifier = modifier
            .alpha(0.95f)
            .fillMaxWidth()
            .selectableGroup(),
        color = basicWhite,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp)
        ) {
            NekoAnimeScreen.values().forEach { destination ->
                BottomAppBarItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    selected = destination.route == currentDestination?.route,
                    onClick = { onNavigateTo(destination) },
                    iconId = destination.iconId,
                    label = destination.label,
                )
            }
        }
    }
}

@Composable
fun BottomAppBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    iconId: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    val animatedIcon by rememberLottieComposition(LottieCompositionSpec.RawRes(iconId))
    val animationProgress: Float by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(800, easing = LinearEasing)
    )

    Box(
        modifier.selectable(
            selected = selected,
            onClick = onClick,
            role = Role.Tab,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LottieAnimation(animatedIcon, progress = {
                if (selected) animationProgress else 0f // 仅进入动画有过渡效果
            })
            Text(
                text = label,
                fontSize = TextUnit(9f, TextUnitType.Sp),
                color = if (selected) pink50 else darkPink30
            )
        }
    }
}