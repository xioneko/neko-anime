package com.xioneko.android.nekoanime.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeFontFamilies
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicBlack
import com.xioneko.android.nekoanime.ui.theme.pink95

@Composable
fun SolidTopBar(
    title: String,
    leftIconId: Int = NekoAnimeIcons.arrowLeft,
    onLeftIconClick: () -> Unit = {},
    rightIconId: Int? = null,
    onRightIconClick: (() -> Unit) = {},
) {
    val systemUiController = rememberSystemUiController()
    val interactionResource = remember { MutableInteractionSource() }
    LaunchedEffect(Unit) {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = true
        )
    }

    Surface {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                )
                .padding(15.dp, 12.dp),
        ) {
            Icon(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable(
                        interactionSource = interactionResource,
                        indication = null,
                        role = Role.Button,
                        onClick = onLeftIconClick
                    ),
                painter = painterResource(leftIconId),
                contentDescription = null,
            )
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            if (rightIconId != null) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable(
                            interactionSource = interactionResource,
                            indication = null,
                            role = Role.Button,
                            onClick = onRightIconClick
                        ),
                    painter = painterResource(rightIconId),
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
fun TransparentTopBar(
    title: String,
    iconId: Int,
    iconTint: Color = basicBlack,
    onIconClick:() -> Unit
) {
    val systemUiController = rememberSystemUiController()
    LaunchedEffect(Unit) {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = true
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(20.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontFamily = NekoAnimeFontFamilies.heiFontFamily,
            fontWeight = FontWeight.Normal,
            style = MaterialTheme.typography.titleLarge
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(pink95)
                .clickable(role = Role.Button, onClick = onIconClick)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconId),
                contentDescription = null,
                tint = iconTint
            )
        }
    }
}
