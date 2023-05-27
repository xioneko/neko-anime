package com.xioneko.android.nekoanime.ui.component

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.xioneko.android.nekoanime.ui.theme.neutral10
import com.xioneko.android.nekoanime.ui.theme.pink95

@Composable
fun NekoAnimeSnackBar(
    modifier: Modifier = Modifier,
    snackbarData: SnackbarData,
) {
    Snackbar(
        modifier = modifier,
        snackbarData = snackbarData,
        containerColor = pink95,
        contentColor = neutral10,
    )
}

@Composable
fun NekoAnimeSnackbarHost(
    modifier: Modifier = Modifier,
    visible: Boolean,
    message: () -> String,
    duration: SnackbarDuration = SnackbarDuration.Short,
    snackbar: @Composable (SnackbarData) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(visible) {
        if (visible) {
            snackbarHostState.showSnackbar(
                message = message(),
                duration = duration,
            )
        }
    }

    SnackbarHost(
        modifier = modifier,
        hostState = snackbarHostState,
        snackbar = snackbar
    )
}