package com.xioneko.android.nekoanime.ui.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.random.Random

//AnimeSource
const val KEY_SOURCE_MODE = "animeSourceMode"


// danmaku
const val KEY_DANMAKU_ENABLED = "danmakuEnabled"
const val KEY_DANMAKU_CONFIG_DATA = "danmakuConfigData"

@Composable
fun currentScreenSizeDp(): Pair<Int, Int> {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        configuration.screenWidthDp to configuration.screenHeightDp
    }
}

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
    return remember(configuration) {
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            configuration.screenHeightDp > 600
        } else {
            configuration.screenWidthDp > 600
        }
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


fun vibrate(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    vibrator.vibrate(VibrationEffect.createOneShot(100, 16))
}

fun <T> List<T>.getRandomElements(n: Int): List<T> {
    if (size <= n) return this
    val result = toMutableList()
    for (i in 0 until n) {
        val randomIndex = i + Random.nextInt(result.size - i)
        result[i] = result[randomIndex].also { result[randomIndex] = result[i] }
    }
    return result.take(n)
}

fun Context.getScreenBrightness(): Float =
    if (this is Activity && window.attributes.screenBrightness >= 0) {
        window.attributes.screenBrightness
    } else {
        try {
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255f
        } catch (e: Settings.SettingNotFoundException) {
            0.3f
        }
    }

fun Context.resetScreenBrightness() {
    if (this !is Activity) return
    val layoutParams = window.attributes
    layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    window.attributes = layoutParams
}

fun Context.setScreenBrightness(brightness: Float) {
    if (this !is Activity) return
    val layoutParams = window.attributes
    layoutParams.screenBrightness = brightness
    window.attributes = layoutParams
}

val Context.preferences: SharedPreferences
    get() = getSharedPreferences("preferences", Context.MODE_PRIVATE)

@Composable
fun rememberPreference(key: String, defaultValue: Boolean): MutableState<Boolean> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.preferences.getBoolean(key, defaultValue)) {
            context.preferences.edit { putBoolean(key, it) }
        }
    }
}

@Composable
inline fun <reified T : Enum<T>> rememberPreference(key: String, defaultValue: T): MutableState<T> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.preferences.getEnum(key, defaultValue)) {
            context.preferences.edit { putEnum(key, it) }
        }
    }
}

inline fun <reified T : Enum<T>> SharedPreferences.getEnum(
    key: String,
    defaultValue: T
): T =
    getString(key, null)?.let {
        try {
            enumValueOf<T>(it)
        } catch (e: IllegalArgumentException) {
            null
        }
    } ?: defaultValue

inline fun <reified T : Enum<T>> SharedPreferences.Editor.putEnum(
    key: String,
    value: T
): SharedPreferences.Editor =
    putString(key, value.name)

fun <T> SharedPreferences.getObject(
    key: String,
    defaultValue: T,
    serializer: KSerializer<T>
): T {
    val json = getString(key, null) ?: return defaultValue
    return try {
        Json.decodeFromString(serializer, json)
    } catch (e: Exception) {
        defaultValue
    }
}

fun <T> SharedPreferences.Editor.putObject(
    key: String,
    value: T,
    serializer: KSerializer<T>
): SharedPreferences.Editor {
    val json = Json.encodeToString(serializer, value)
    return putString(key, json)
}


@Composable
fun <T> rememberPreference(
    key: String,
    defaultValue: T,
    serializer: KSerializer<T>
): MutableState<T> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.preferences.getObject(key, defaultValue, serializer)) {
            context.preferences.edit { putObject(key, it, serializer) }
        }
    }
}

inline fun <T> mutableStatePreferenceOf(
    value: T,
    crossinline onStructuralInequality: (newValue: T) -> Unit
) = mutableStateOf(
    value = value,
    policy = object : SnapshotMutationPolicy<T> {
        override fun equivalent(a: T, b: T): Boolean {
            val areEquals = a == b
            if (!areEquals) onStructuralInequality(b)
            return areEquals
        }
    })

private fun Context.getAudioManager() = getSystemService(Context.AUDIO_SERVICE) as AudioManager


fun Context.getMediaVolume(): Float {
    val audioManager = getAudioManager()
    return audioManager.run {
        getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }
}

fun Context.setMediaVolume(volume: Float) {
    val audioManager = getAudioManager()
    try {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            (volume * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)).toInt(),
            AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
        )
    } catch (e: SecurityException) {
        Log.w("Player", "Failed to change volume: $e")
    }
}

fun testConnection(client: OkHttpClient, url: String) = callbackFlow {
    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: Call, e: IOException) {
            channel.trySend(false)
        }

        override fun onResponse(call: Call, response: Response) {
            channel.trySend(response.isSuccessful)
        }
    })

    awaitClose {}
}