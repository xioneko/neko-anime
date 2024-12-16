package com.xioneko.android.nekoanime.data.network.danmu.api

import com.xioneko.android.nekoanime.data.network.danmu.dto.Danmuku
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface DanmuSession {
    val totalCount: Flow<Int?> get() = emptyFlow()

    fun at(
        curTimeMillis: () -> Duration,
        isPlayingFlow: Flow<Boolean> = flowOf(true)
    ): Flow<DanmuEvent>
}

sealed class DanmuEvent {
    class Add(val danmu: Danmuku) : DanmuEvent()

    /**
     * 清空屏幕并以这些弹幕填充. 常见于快进/快退时
     *
     * @param list 顺序为由距离当前时间近到远.
     * @param playTimeMillis 当前播放器的时间
     */
    data class Repopulate(val list: List<Danmuku>, val playTimeMills: Long) : DanmuEvent()
}

class TimeBasedDanmuSession private constructor(
    private val list: List<Danmuku>,
    private val flowCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    private val tickDelay: Long = 400
) : DanmuSession {
    override val totalCount: Flow<Int?>
        get() = flowOf(list.size)

    companion object {
        fun create(
            sequence: Sequence<Danmuku>,
            coroutineContext: CoroutineContext = EmptyCoroutineContext
        ): DanmuSession {
            val list = sequence.mapTo(ArrayList()) { sanitize(it) }
            list.sortBy { it.playTimeMillis }
            return TimeBasedDanmuSession(list, coroutineContext)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun at(
        curTimeMillis: () -> Duration,
        isPlayingFlow: Flow<Boolean>
    ): Flow<DanmuEvent> {
        if (list.isEmpty()) {
            return emptyFlow()
        }
        val state = DanmuSessionFlowState(
            list,
            repolateThresholed = 3.seconds,
            repopulateDistance = { 10.seconds }
        )
        val algorithm = DanmuSessionAlgorithm(state)
        return isPlayingFlow.flatMapLatest { isPlaying ->
            channelFlow {
                launch(Dispatchers.Main) {
                    while (isActive && isPlaying) {
                        state.curTimeShared = curTimeMillis()
                        delay(tickDelay)
                    }
                }
                val sendItem: (DanmuEvent) -> Boolean = {
                    trySend(it).isSuccess
                }

                while (isActive && isPlaying) {
                    algorithm.tick(sendItem)
                    delay(tickDelay)
                }
            }.flowOn(flowCoroutineContext)
        }
    }
}

internal class DanmuSessionAlgorithm(val state: DanmuSessionFlowState) {
    private inline fun userEachDanmu(block: (Danmuku) -> Unit) {
        var i = state.lastIndex + 1
        val list = state.list
        try {
            while (i <= list.lastIndex) {
                block(list[i])
                i++
            }
        } finally {
            state.lastIndex = i - 1
        }
    }

    fun tick(sendEvent: (DanmuEvent) -> Boolean) {
        val curTime = state.curTimeShared
        if (curTime == Duration.INFINITE) {
            return
        }
        val list = state.list
        try {
            if (state.lastTime == Duration.INFINITE || (curTime - state.lastTime).absoluteValue >= state.repolateThresholed) {
                val targetTime = (curTime - state.repopulateDistance()).inWholeSeconds
                state.lastIndex = list
                    .binarySearchBy(targetTime, selector = { it.playTimeMillis })
                    .let {
                        if (it >= 0) {
                            if (list[it].playTimeMillis < targetTime) {
                                it + 1
                            } else it
                        } else -(it + 1) - 1
                    }
                    .coerceAtLeast(-1)
                //发送范围内所有弹幕
                val curTimeMills = curTime.inWholeSeconds
                val event = DanmuEvent.Repopulate(
                    buildList ss@{
                        var emitted = 0
                        userEachDanmu { item ->
                            if (curTimeMills < item.playTimeMillis) {
                                return@ss
                            }
                            if (emitted >= state.repopulateMaxCount) {
                                return@ss
                            }
                            add(item)
                            emitted++
                        }
                    },
                    curTimeMills
                )
                sendEvent(event)
                return
            }
        } finally {
            state.lastTime = curTime
        }
        val curTimeMillis = curTime.inWholeSeconds
        userEachDanmu { item ->
            if (curTimeMillis < item.playTimeMillis) {
                return
            }
            if (!sendEvent(DanmuEvent.Add(item))) {
                return
            }
        }

    }


}

internal class DanmuSessionFlowState(
    val list: List<Danmuku>,
    /**
     * 当前播放进度
     */
    @Volatile var curTimeShared: Duration = Duration.INFINITE,
    /**
     * 快进超过阙值,重新装填
     */
    val repolateThresholed: Duration = 3.seconds,
    /**
     * 装填范围
     */
    val repopulateDistance: () -> Duration,
    val repopulateMaxCount: Int = 40
) {
    var lastTime: Duration = Duration.INFINITE

    /**
     * 最后成功发送了的弹幕的索引
     */
    var lastIndex = -1
}

private fun sanitize(danmu: Danmuku) = danmu.run {
    if (text.indexOf("\n") == -1) return@run danmu

    copy(
        text = text
            .replace("\r\n", " ")
            .replace("\n\r", " ")
            .replace("\n", " ")
            .trim()
    )

}

