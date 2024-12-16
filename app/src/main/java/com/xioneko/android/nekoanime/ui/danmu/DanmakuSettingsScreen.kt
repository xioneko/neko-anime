package com.xioneko.android.nekoanime.ui.danmu

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable

@Serializable
data class DanmakuConfigData(
    val enableTop: Boolean = true,
    val enableFloating: Boolean = true,
    val enableBottom: Boolean = true,
    val enableColor: Boolean = true,
    val speed: Float = DanmakuConfig.Default.baseSpeed,
    val safeSeparation: Float = DanmakuConfig.Default.safeSeparation.value,
    val displayArea: Float = DanmakuConfig.Default.displayArea,
    val alpha: Float = DanmakuConfig.Default.style.alpha,
    val fontSize: Float = DanmakuConfig.Default.style.fontSize.value,
    val fontWeight: Int = DanmakuConfig.Default.style.fontWeight.weight,
    val strokeWidth: Float = DanmakuConfig.Default.style.strokeWidth,
    // val strokeColor: ULong = DanmakuConfig.Default.style.strokeColor.value,
//     val isDebug: Boolean = true,
) {
    companion object {
        val Default = DanmakuConfigData()
    }

    fun toDanmakuConfig(): DanmakuConfig {
        return DanmakuConfig(
            enableTop = enableTop,
            enableFloating = enableFloating,
            enableBottom = enableBottom,
            enableColor = enableColor,
            displayArea = displayArea,
            baseSpeed = speed,
            style = DanmakuStyle.Default.copy(
                alpha = alpha,
                fontSize = fontSize.sp,
                fontWeight = FontWeight(fontWeight),
                strokeWidth = strokeWidth
            ),
        )
    }
}
