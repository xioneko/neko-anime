package com.xioneko.android.nekoanime.ui.component

import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.xioneko.android.nekoanime.R
import com.xioneko.android.nekoanime.data.network.di.NekoAnimeMode
import com.xioneko.android.nekoanime.data.network.di.SourceHolder
import com.xioneko.android.nekoanime.data.network.di.SourceHolder.DEFAULT_ANIME_SOURCE
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeFontFamilies.cuteFontFamily
import com.xioneko.android.nekoanime.ui.theme.basicBlack
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.theme.pink40
import com.xioneko.android.nekoanime.ui.theme.pink95
import com.xioneko.android.nekoanime.ui.util.KEY_SOURCE_MODE
import com.xioneko.android.nekoanime.ui.util.isTablet
import com.xioneko.android.nekoanime.ui.util.rememberPreference

@Composable
fun WorkingInProgressDialog(
    onDismiss: () -> Unit,
) {
    AlertDialog(
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("了解了", color = pink40)
            }
        },
        onDismissRequest = onDismiss,
        icon = {
            Image(
                painter = painterResource(R.drawable.working_in_progress),
                contentDescription = "working in progress"
            )
        },
        title = {
            Text(
                text = "此项功能正在开发中",
                fontFamily = cuteFontFamily,
                color = basicBlack,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        containerColor = pink95,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDialog(
    versionName: String,
    updateNotes: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isTablet = isTablet()

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .requiredHeightIn(
                max = min(
                    if (isTablet) 540.dp else 360.dp,
                    LocalConfiguration.current.screenHeightDp.dp * 4 / 5
                )
            )
            .clip(RoundedCornerShape(12.dp))
            .background(basicWhite)
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box {
                Image(
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth,
                    painter = painterResource(R.drawable.update_popover_header_bg),
                    contentDescription = "header"
                )
                Column(
                    modifier = Modifier.padding(start = 18.dp, top = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "发现新版本",
                        color = basicWhite,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "v$versionName",
                        color = basicWhite,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            val hPadding = with(LocalDensity.current) { 15.dp.roundToPx() }
            val vPadding = with(LocalDensity.current) { 12.dp.roundToPx() }
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                factory = { context ->
                    TextView(context).apply {
                        setLineSpacing(20f, 1f)
                        setPadding(hPadding, vPadding, hPadding, 0)
                    }
                },
                update = {
                    it.text = HtmlCompat.fromHtml(
                        updateNotes ?: "优化应用体验，修复若干问题",
                        HtmlCompat.FROM_HTML_MODE_COMPACT
                    )
                }
            )
            FilledTonalButton(
                modifier = Modifier.padding(top = 12.dp),
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = pink40,
                    contentColor = basicWhite
                ),
                contentPadding = PaddingValues(64.dp, 6.dp)
            ) {
                Text(text = "立即升级", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationDialog(
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .height(96.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(basicWhite)
            .padding(start = 20.dp, end = 20.dp, top = 15.dp, bottom = 12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = text,
                color = basicBlack,
                style = MaterialTheme.typography.bodyLarge
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                Text(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onDismiss
                    ),
                    text = "取消",
                    color = Color(0xFFD49DB0),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onConfirm
                    ),
                    text = "确定",
                    color = Color(0xFFD46E92),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


//切换数据源
@Composable
fun SourceSwitchDialog(
    onDismissRequest: (Boolean) -> Unit,
    animeDataSource: String?
) {
    var currentSourceMode by rememberPreference(KEY_SOURCE_MODE, DEFAULT_ANIME_SOURCE)
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(currentSourceMode.name) }

    val radioMap = NekoAnimeMode.entries.map { it.name }
    AlertDialog(
        onDismissRequest = {
            onDismissRequest(false)
        },
        title = {
            Text(text = "切换动漫源")
        },
        text = {
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                radioMap.forEachIndexed { index, text ->
                    val topCorner = if (index == 0) 24.dp else 4.dp
                    val bottomCorner = if (index == radioMap.lastIndex) 24.dp else 4.dp
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.radio_button_height))
                            .clip(
                                RoundedCornerShape(
                                    topStart = topCorner,
                                    topEnd = topCorner,
                                    bottomStart = bottomCorner,
                                    bottomEnd = bottomCorner
                                )
                            )
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .selectable(
                                selected = (text == selectedOption),
                                onClick = {
                                    onOptionSelected(text)
                                },
                                role = Role.RadioButton
                            )
                            .padding(start = dimensionResource(id = R.dimen.large_padding)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (text == selectedOption),
                            onClick = null
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.medium_padding))
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val mode = NekoAnimeMode.valueOf(selectedOption)
                    currentSourceMode = mode
                    SourceHolder.isSourceChanged = true
                    SourceHolder.switchSource(mode)
                    onDismissRequest(false)
                }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissRequest(false)
            }) {
                Text("取消")
            }
        }


    )

}


