package com.xioneko.android.nekoanime.ui.util

import android.content.res.Configuration
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

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
    }.also {
        Log.d("UI", "tablet? $it")
    }
}