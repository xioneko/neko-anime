package com.xioneko.android.nekoanime.ui.danmu

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.delay

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DanmakuHost(
    state: DanmakuHostState,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier) {
        //TODO 状态丢失
        state.hostHeight = 1080
        state.hostWidth = 2400
        DanmakuCanvas {
            for (danmaku in state.presentFloatingDanmaku) {
                with(danmaku.danmaku) {
                    draw(
                        screenPosX = { danmaku.screenPosX },
                        screenPosY = { danmaku.screenPosY },
                    )
                }
            }
            for (danmaku in state.presentFixedDanmaku) {
                with(danmaku.danmaku) {
                    draw(
                        screenPosX = { danmaku.screenPosX },
                        screenPosY = { danmaku.screenPosY },
                    )
                }
            }
        }
    }

    // set the number of tracks
    LaunchedEffect(state.hostHeight) { state.setTrackCount() }
    // calculate current play time on every frame
    LaunchedEffect(state.paused) { if (!state.paused) state.interpolateFrameLoop() }
    // logical tick for removal of danmaku
    LaunchedEffect(state.paused) {
        if (!state.paused) {
            while (true) {
                state.tick()
                delay(1000)
            }
        }
    }


}

@Composable
fun DanmakuCanvas(modifier: Modifier = Modifier, onDraw: DrawScope.() -> Unit) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        onDraw()
    }
}



