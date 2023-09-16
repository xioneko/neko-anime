package com.xioneko.android.nekoanime.ui

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.xioneko.android.nekoanime.ui.theme.NekoAnimeTheme
import com.xioneko.android.nekoanime.ui.util.setScreenOrientation
import com.xioneko.android.nekoanime.util.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Activity initial creation
        if (savedInstanceState == null) {
            registerUncaughtExceptionHandler()

            lifecycleScope.launch {
                if (viewModel.isLandscapeModeDisabled())
                    setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            }
        }

        splashScreen.setKeepOnScreenCondition { viewModel.isSplashScreenVisible.value }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            NekoAnimeTheme {
                NekoAnimeApp(networkMonitor, viewModel.updater)
            }
        }
    }

    private fun registerUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            Log.e("GLOBAL", throwable.message.toString())

            CoroutineScope(Dispatchers.Default).launch {
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("(T ^ T) 崩溃了...")
                        .setMessage("Neko Anime 遇到了一个错误，是否要重启应用？")
                        .setPositiveButton("重启") { _, _ ->
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            applicationContext.startActivity(intent)
                            exitProcess(0)
                        }
                        .setNegativeButton("退出") { _, _ ->
                            exitProcess(1)
                        }
                        .setCancelable(false)
                        .show()
                }
            }
        }
    }
}