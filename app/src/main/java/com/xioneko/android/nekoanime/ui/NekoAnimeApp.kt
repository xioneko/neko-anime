package com.xioneko.android.nekoanime.ui

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.xioneko.android.nekoanime.navigation.NekoAnimeNavigationGraph
import com.xioneko.android.nekoanime.ui.component.UpdateDialog
import com.xioneko.android.nekoanime.ui.theme.basicWhite
import com.xioneko.android.nekoanime.util.NekoAnimeUpdater
import com.xioneko.android.nekoanime.util.NetworkMonitor

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NekoAnimeApp(
    networkMonitor: NetworkMonitor,
    updater: NekoAnimeUpdater
) {
    val context = LocalContext.current
    val isOffline by networkMonitor.isOffline.collectAsStateWithLifecycle(null)
    val navController: NavHostController = rememberNavController()

    val isUpdateAvailable by updater.isUpdateAvailable.collectAsStateWithLifecycle()
    var shouldShowUpdateDialog by rememberSaveable(isUpdateAvailable) { mutableStateOf(isUpdateAvailable) }

    LaunchedEffect(isOffline) {
        if (isOffline == true) {
            Toast.makeText(context, "网络似乎不在状态", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier,
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