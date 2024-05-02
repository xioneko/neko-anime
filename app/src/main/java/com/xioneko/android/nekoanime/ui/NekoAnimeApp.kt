package com.xioneko.android.nekoanime.ui

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.xioneko.android.nekoanime.navigation.NekoAnimeNavigationGraph
import com.xioneko.android.nekoanime.ui.component.NekoAnimeSnackBar
import com.xioneko.android.nekoanime.ui.component.NekoAnimeSnackbarHost
import com.xioneko.android.nekoanime.ui.component.UpdateDialog
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.util.NekoAnimeUpdater
import com.xioneko.android.nekoanime.util.NetworkMonitor

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NekoAnimeApp(
    networkMonitor: NetworkMonitor,
    updater: NekoAnimeUpdater
) {
    val context = LocalContext.current
    val isOffline by networkMonitor.isOffline.collectAsStateWithLifecycle(true)
    val navController: NavHostController = rememberNavController()

    val isUpdateAvailable by updater.isUpdateAvailable.collectAsStateWithLifecycle()
    var shouldShowUpdateDialog by rememberSaveable(isUpdateAvailable) { mutableStateOf(isUpdateAvailable) }

    Scaffold(
        modifier = Modifier,
        snackbarHost = {
            NekoAnimeSnackbarHost(
                visible = isOffline,
                message = { "🥹 网络似乎不在状态..." }
            ) {
                NekoAnimeSnackBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .requiredWidth(220.dp),
                    snackbarData = it
                )
            }
        },
        containerColor = basicWhite,
        contentWindowInsets = WindowInsets(0),
    ) {
        NekoAnimeNavigationGraph(navController = navController)

        if (shouldShowUpdateDialog) {
            UpdateDialog(
                versionName = updater.latestVersion.toString(),
                updateNotes = updater.updateNotes,
                onConfirm = {
                    shouldShowUpdateDialog = false
                    updater.openDownloadLink(context)
                },
                onDismiss = { shouldShowUpdateDialog = false }
            )
        }
    }
}