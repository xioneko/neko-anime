package com.xioneko.android.nekoanime.ui.danmu

import android.annotation.SuppressLint
import android.util.Log
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
        state.hostHeight = 1000
        state.hostWidth = 2000
        Log.d("danmu", "旋转屏幕大小" + state.hostWidth)
        Log.d("danmu", "旋转屏幕大小" + state.hostHeight)
        DanmakuCanvas {
            Log.d("danmu", "弹幕列表数量1:" + state.presentFloatingDanmaku.size)
            for (danmaku in state.presentFloatingDanmaku) {
                with(danmaku.danmaku) {
                    Log.d("danmu", "打印弹幕1")
                    //TODO 打印不到播放器上面
                    draw(
                        screenPosX = { danmaku.screenPosX },
                        screenPosY = { danmaku.screenPosY },
                    )
                }
            }
            Log.d("danmu", "弹幕列表数量2:" + state.presentFixedDanmaku.size)
            for (danmaku in state.presentFixedDanmaku) {
                with(danmaku.danmaku) {
                    Log.d("danmu", "打印弹幕2")
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



