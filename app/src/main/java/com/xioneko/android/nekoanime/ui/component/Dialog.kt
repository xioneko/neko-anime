package com.xioneko.android.nekoanime.ui.component

import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.xioneko.android.nekoanime.R
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeFontFamilies.cuteFontFamily
import com.xioneko.android.nekoanime.ui.theme.basicBlack
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.ui.theme.pink40
import com.xioneko.android.nekoanime.ui.theme.pink95

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
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(basicWhite),
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
                    .heightIn(min = 120.dp, max = 270.dp)
                    .verticalScroll(rememberScrollState()),
                factory = { context ->
                    TextView(context).apply {
                        setLineSpacing(20f, 1f)
                    }
                },
                update = {
                    it.text = HtmlCompat.fromHtml(
                        updateNotes ?: "优化产品体验，修复若干问题",
                        HtmlCompat.FROM_HTML_MODE_COMPACT
                    )
                }
            )
            FilledTonalButton(
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