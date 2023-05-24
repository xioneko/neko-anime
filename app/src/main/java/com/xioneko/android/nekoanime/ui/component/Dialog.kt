package com.xioneko.android.nekoanime.ui.component

import androidx.compose.foundation.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.xioneko.android.nekoanime.R
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeFontFamilies.cuteFontFamily
import com.xioneko.android.nekoanime.ui.theme.basicBlack
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