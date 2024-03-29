package com.xioneko.android.nekoanime.ui.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView

@Composable
fun getAspectRadio(): Float {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        configuration.screenHeightDp.toFloat() / configuration.screenWidthDp.toFloat()
    }
}

@Composable
fun isTablet(): Boolean {
    val configuration = LocalConfiguration.current
    return if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        configuration.screenHeightDp > 600
    } else {
        configuration.screenWidthDp > 600
    }
}

fun Context.setScreenOrientation(orientation: Int) {
    val activity = this as? Activity ?: return
    activity.requestedOrientation = orientation
}

fun Context.isOrientationLocked() =
    Settings.System.getInt(
        contentResolver,
        Settings.System.ACCELEROMETER_ROTATION,
        1
    ) == 0

@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}

