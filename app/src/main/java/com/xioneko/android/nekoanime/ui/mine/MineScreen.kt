package com.xioneko.android.nekoanime.ui.mine

import android.content.pm.ActivityInfo
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xioneko.android.nekoanime.data.model.ThemeConfig
import com.xioneko.android.nekoanime.ui.component.AnimatedSwitchButton
import com.xioneko.android.nekoanime.ui.component.NekoAnimeSnackBar
import com.xioneko.android.nekoanime.ui.component.TransparentTopBar
import com.xioneko.android.nekoanime.ui.component.WorkingInProgressDialog
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeIcons
import com.xioneko.android.nekoanime.ui.theme.basicBlack
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.theme.pink10
import com.xioneko.android.nekoanime.ui.theme.pink50
import com.xioneko.android.nekoanime.ui.theme.pink99
import com.xioneko.android.nekoanime.ui.util.getAspectRadio
import com.xioneko.android.nekoanime.ui.util.isTablet
import com.xioneko.android.nekoanime.ui.util.setScreenOrientation
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
    val context = LocalContext.current
    val isTablet = isTablet()
    val aspectRadio = getAspectRadio()

    val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
    val isSystemInDarkTheme = isSystemInDarkTheme()

    val updateAutoCheck by viewModel.updateAutoCheck.collectAsStateWithLifecycle()

    val disableLandscapeMode by viewModel.disableLandscapeMode.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var showWorkingInProgressDialog by remember { mutableStateOf(false) }
    if (showWorkingInProgressDialog) {
        WorkingInProgressDialog {
            showWorkingInProgressDialog = false
        }
    }

    Scaffold(
        modifier = Modifier.padding(padding),
        topBar = {
            TransparentTopBar(
                title = greeting(LocalTime.now()),
                iconId = NekoAnimeIcons.light,
                onIconClick = { /* TODO: 主题模式切换 */  showWorkingInProgressDialog = true }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            ) {
                NekoAnimeSnackBar(
                    modifier = Modifier.requiredWidth(210.dp),
                    snackbarData = it
                )
            }
        },
        containerColor = pink99,
        contentWindowInsets = WindowInsets(0)
    ) {

        val cardModifier = remember {
            Modifier
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(6.dp),
                    ambientColor = pink50.copy(0.2f),
                    spotColor = pink50.copy(0.2f)
                )
                .padding(1.dp)
                .clip(RoundedCornerShape(6.dp))
        }

        val itemModifier = remember {
            Modifier
                .fillMaxWidth()
                .height(if (isTablet) 48.dp else 38.dp)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(rememberScrollState())
                .padding(15.dp, 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (aspectRadio > 1.8) 12.dp else 28.dp)
            ) {
                PrimaryBox(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(if (aspectRadio > 1.8) 1.25f else 1.8f)
                        .then(cardModifier),
                    iconId = NekoAnimeIcons.love,
                    text = "我的追番",
                    onClick = onFollowedAnimeClick
                )
                PrimaryBox(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(if (aspectRadio > 1.8) 1.25f else 1.8f)
                        .then(cardModifier),
                    iconId = NekoAnimeIcons.history,
                    text = "历史观看",
                    onClick = onHistoryClick
                )
                PrimaryBox(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(if (aspectRadio > 1.8) 1.25f else 1.8f)
                        .then(cardModifier),
                    iconId = NekoAnimeIcons.download,
                    text = "我的下载",
                    onClick = { showWorkingInProgressDialog = true }
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(cardModifier)
            ) {
                ItemWithNewPage(
                    modifier = itemModifier,
                    text = "重新选择我的兴趣点",
                    onClick = {
                        /* TODO：兴趣点选择 */
                        showWorkingInProgressDialog = true
                    }
                )

                ItemWithSwitch(
                    modifier = itemModifier,
                    text = "夜间主题跟随系统",
                    checked = if (themeConfig == null) null else
                        themeConfig == ThemeConfig.THEME_CONFIG_FOLLOW_SYSTEM,
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

                ItemWithSwitch(
                    modifier = itemModifier,
                    text = "自动检查更新",
                    checked = updateAutoCheck,
                    onCheckedChange = viewModel::setUpdateAutoCheck
                )

                ItemWithSwitch(
                    modifier = itemModifier,
                    text = "禁用横屏模式",
                    checked = disableLandscapeMode,
                    onCheckedChange = { disable ->
                        viewModel.setDisableLandscapeMode(disable)
                        if (disable)
                            context.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        else
                            context.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    }
                )

                ItemWithAction(
                    modifier = itemModifier,
                    text = "清除番剧数据缓存",
                    action = {
                        viewModel.clearAnimeCache(
                            onFinished = {
                                snackbarHostState.showSnackbar(
                                    message = "已清除全部番剧数据缓存",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        )
                    }
                )

                ItemWithAction(
                    modifier = itemModifier,
                    text = "访问 GitHub 仓库",
                    action = { viewModel.accessGitHubRepo(context) }
                )
            }
        }
    }
}

@Composable
private fun PrimaryBox(
    modifier: Modifier,
    iconId: Int,
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        contentColor = basicBlack,
        color = basicWhite
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
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
}

@Composable
private fun ItemWithNewPage(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(basicWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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

}

@Composable
private fun ItemWithSwitch(
    modifier: Modifier = Modifier,
    text: String,
    checked: Boolean?,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .background(basicWhite)
            .clickable { onCheckedChange(checked?.not() ?: false) }
            .padding(horizontal = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
            )
            AnimatedSwitchButton(
                modifier = Modifier.size(32.dp),
                checked = checked
            )
        }
    }

}

@Composable
private fun ItemWithAction(
    modifier: Modifier = Modifier,
    text: String,
    action: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(basicWhite)
            .clickable(onClick = action)
            .padding(horizontal = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
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