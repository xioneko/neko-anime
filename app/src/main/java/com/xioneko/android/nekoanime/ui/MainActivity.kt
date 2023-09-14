package com.xioneko.android.nekoanime.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeTheme
import com.xioneko.android.nekoanime.util.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { viewModel.isSplashScreenVisible.value }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            NekoAnimeTheme {
                NekoAnimeApp(networkMonitor, viewModel.updater)
            }
        }
    }
}