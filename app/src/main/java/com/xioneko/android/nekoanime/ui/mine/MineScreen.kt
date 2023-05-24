package com.xioneko.android.nekoanime.ui.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xioneko.android.nekoanime.data.model.ThemeConfig
import com.xioneko.android.nekoanime.ui.component.TransparentTopBar
import com.xioneko.android.nekoanime.ui.component.WorkingInProgressDialog
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.theme.pink10
import com.xioneko.android.nekoanime.ui.theme.pink50
import com.xioneko.android.nekoanime.ui.theme.pink99
import java.time.LocalTime

@Composable
fun MineScreen(
    padding: PaddingValues,
    viewModel: MineScreenViewModel = hiltViewModel(),
    onDownloadClick: () -> Unit,
    onFollowedAnimeClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onAnimeClick: (Int) -> Unit,
) {
    val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
    val isSystemInDarkTheme = isSystemInDarkTheme()

    var showWorkingInProgressDialog by remember { mutableStateOf(false) }
    if (showWorkingInProgressDialog) {
        WorkingInProgressDialog {
            showWorkingInProgressDialog = false
        }
    }

    Scaffold(
        modifier = Modifier
            .padding(padding)
            .background(pink99),
        topBar = {
            TransparentTopBar(
                title = greeting(LocalTime.now()),
                iconId = NekoAnimeIcons.light,
                onIconClick = { /* TODO: 主题模式切换 */  showWorkingInProgressDialog = true }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) {

        val blockModifier = Modifier
            .padding(6.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(6.dp),
                ambientColor = pink50.copy(0.2f),
                spotColor = pink50.copy(0.2f)
            )
            .padding(1.dp)
            .background(
                basicWhite,
                RoundedCornerShape(6.dp)
            )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(rememberScrollState())
                .padding(15.dp, 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PrimaryBox(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(blockModifier),
                    iconId = NekoAnimeIcons.love,
                    text = "我的追番",
                    onClick = onFollowedAnimeClick
                )
                PrimaryBox(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(blockModifier),
                    iconId = NekoAnimeIcons.history,
                    text = "历史观看",
                    onClick = onHistoryClick
                )
                PrimaryBox(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(blockModifier),
                    iconId = NekoAnimeIcons.download,
                    text = "我的下载",
                    onClick = { showWorkingInProgressDialog = true }
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(blockModifier),
            ) {

                ItemWithNewPage(
                    text = "重新选择我的兴趣点",
                    onClick = {
                        /* TODO：兴趣点选择 */
                        showWorkingInProgressDialog = true
                    }
                )

                ItemWithSwitch(
                    text = "夜间主题跟随系统",
                    checked = themeConfig == ThemeConfig.THEME_CONFIG_FOLLOW_SYSTEM,
                    onCheckedChange = { followSystem ->
                        viewModel.setTheme(
                            if (followSystem) {
                                ThemeConfig.THEME_CONFIG_FOLLOW_SYSTEM
                            } else if (isSystemInDarkTheme) {
                                ThemeConfig.THEME_CONFIG_DARK
                            } else {
                                ThemeConfig.THEME_CONFIG_LIGHT
                            }
                        )
                    }
                )

                ItemWithNewPage(
                    text = "关于",
                    onClick = { /* TODO: 关于页面 */ showWorkingInProgressDialog = true }
                )
            }
        }
    }
}

@Composable
fun PrimaryBox(
    modifier: Modifier,
    iconId: Int,
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .aspectRatio(5f / 4f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(1.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(iconId),
                contentDescription = text,
                tint = pink10,
            )
            Text(
                text = text,
                color = pink10,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun ItemWithNewPage(
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(15.dp, 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
        Icon(
            painter = painterResource(NekoAnimeIcons.arrowRight),
            contentDescription = "more",
        )
    }
}

@Composable
private fun ItemWithSwitch(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(15.dp, 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )
        // TODO: Switch ICON
    }
}

private fun greeting(currentTime: LocalTime) = currentTime.run {
    when {
        isBefore(LocalTime.of(6, 0)) -> "晚上好！"
        isBefore(LocalTime.of(8, 30)) -> "早上好！"
        isBefore(LocalTime.NOON) -> "上午好！"
        isBefore(LocalTime.of(13, 0)) -> "中午好！"
        isBefore(LocalTime.of(18, 0)) -> "下午好！"
        else -> "晚上好！"
    }
}