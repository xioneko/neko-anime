package com.xioneko.android.nekoanime.data

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.xioneko.android.nekoanime.R
import com.xioneko.android.nekoanime.data.AnimeDownloadManager.getDownloadNotificationHelper
import com.xioneko.android.nekoanime.ui.MainActivity
import com.xioneko.android.nekoanime.ui.download.deepLinkToDownloadedAnime
import com.xioneko.android.nekoanime.ui.player.deepLinkToAnimePlay
import com.xioneko.android.nekoanime.ui.util.withTracking
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class AnimeDownloadHelper @OptIn(UnstableApi::class)
@Inject constructor(
    @ApplicationContext context: Context,
    private val animeRepository: AnimeRepository,
) {
    companion object {
        const val STOP_REASON_PAUSE = 127
        const val STATE_PREPARING = 127
        const val FAILURE_REASON_SOURCE_ERROR = 127

        /**
         * @return animeId, episode, animeName
         */
        fun parseRequestId(requestId: String): Pair<Int, Int> {
            val (animeId, episode) = requestId.split("-")
            return animeId.toInt() to episode.toInt()
        }

        fun createRequestId(animeId: Int, episode: Int): String {
            return "${animeId}-${episode}"
        }
    }

    private val fetchingVideoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // TODO: 可用存储空间
//    var availableBytesLeft: Long =
//        StatFs(AnimeDownloadManager.getDownloadDirectory(context).path).availableBytes
//        private set

    // animeId -> [episode -> download]
    private val _downloads: MutableStateFlow<Map<Int, Map<Int, DownloadedAnime>>> =
        MutableStateFlow(emptyMap())

    // Note: bytesDownloaded 不会实时更新
    val downloads = _downloads.asStateFlow()

    private fun loadDownloads(context: Context) {
        // Note: 处于 STATE_PREPARING 状态的下载不会被保存
        AnimeDownloadManager.getAllDownloads(context)
            .map {
                val (animeId, episode) = parseRequestId(it.request.id)
                DownloadedAnime(
                    animeId = animeId,
                    episode = episode,
                    state = it.state,
                    stopReason = it.stopReason,
                    failureReason = it.failureReason,
                    bytesDownloaded = it.bytesDownloaded
                ).also {
                    Log.d("Download", "Init Downloads: $it")
                }
            }
            .groupBy { it.animeId }
            .mapValues { (_, downloads) ->
                downloads.associateBy { it.episode }
            }
            .let { x -> _downloads.update { x } }
    }

    private fun upsertDownloadItem(item: DownloadedAnime) {
        Log.d("Download", "Upsert Download: $item")
        _downloads.update {
            it.toMutableMap().apply {
                val episodes = get(item.animeId)?.toMutableMap() ?: mutableMapOf()
                episodes[item.episode] = item
                put(item.animeId, episodes)
            }
        }
    }

    private fun removeDownloadItem(animeId: Int, episode: Int) {
        Log.d("Download", "Remove Download: $animeId, $episode")
        _downloads.update {
            it.toMutableMap().apply {
                get(animeId)?.toMutableMap()?.remove(episode)
                if (get(animeId)?.isEmpty() == true) {
                    remove(animeId)
                }
            }
        }
    }

    init {
        loadDownloads(context)

        AnimeDownloadManager.addListener(context, object : DownloadManager.Listener {
            override fun onDownloadChanged(
                downloadManager: DownloadManager,
                download: Download,
                finalException: Exception?
            ) {
                val (animeId, episode) = parseRequestId(download.request.id)
                upsertDownloadItem(
                    DownloadedAnime(
                        animeId = animeId,
                        episode = episode,
                        state = download.state,
                        stopReason = download.stopReason,
                        failureReason = download.failureReason,
                        bytesDownloaded = download.bytesDownloaded
                    )
                )
            }

            override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
                val (animeId, episode) = parseRequestId(download.request.id)
                removeDownloadItem(animeId, episode)
            }
        })

        AnimeDownloadManager.addListener(
            context,
            TerminalStateNotificationHelper(
                context,
                getDownloadNotificationHelper(context)
            )
        )
    }

    fun downloadProgressFlow(context: Context, animeId: Int, episode: Int): Flow<Float> = flow {
        emit(0f)
        while (currentCoroutineContext().isActive) {
            val download = AnimeDownloadManager.getCurrentDownloads(context)
                .find { it.request.id == createRequestId(animeId, episode) }
            if (download != null) {
                emit(download.percentDownloaded)
            }
            delay(1000)
        }
    }

    @OptIn(UnstableApi::class)
    fun sendRequest(context: Context, animeId: Int, episode: Int) {
        Log.d("Download", "sendRequest: $animeId, $episode")
        suspend fun fetchMediaUri(): Uri? {
            val anime = animeRepository.getAnimeById(animeId)
                .firstOrNull()
                ?: return null

            val sidIterator = anime.streamIds.iterator().withTracking()
            if (!sidIterator.hasNext()) {
                return null
            }

            suspend fun fetchVideoUrl(sid: Int): String? {
                val videoUrl = animeRepository.getVideoUrl(anime, episode, sid).firstOrNull()
                if (videoUrl == null && sidIterator.hasNext()) {
                    val nextSid = sidIterator.next()
                    Log.d("Download", "Try next sid: $nextSid")
                    return fetchVideoUrl(nextSid)
                }
                return videoUrl
            }

            return fetchVideoUrl(sidIterator.next())?.toUri()
        }

        upsertDownloadItem(
            DownloadedAnime(
                animeId = animeId,
                episode = episode,
                state = STATE_PREPARING,
                stopReason = Download.STOP_REASON_NONE,
                failureReason = Download.FAILURE_REASON_NONE,
                bytesDownloaded = 0
            )
        )

        fetchingVideoScope.launch {
            val requestId = createRequestId(animeId, episode)
            val videoUri = fetchMediaUri() ?: run {
                Log.d("Download", "Failed to fetch video uri: $requestId")
                upsertDownloadItem(
                    DownloadedAnime(
                        animeId = animeId,
                        episode = episode,
                        state = Download.STATE_FAILED,
                        stopReason = Download.STOP_REASON_NONE,
                        failureReason = FAILURE_REASON_SOURCE_ERROR,
                        bytesDownloaded = 0
                    )
                )
                return@launch
            }
            Log.d("Download", "Fetched video uri: $videoUri")

            DownloadService.sendAddDownload(
                context,
                AnimeDownloadService::class.java,
                DownloadRequest.Builder(requestId, videoUri).build(),
                false
            )
        }

    }

    fun removeDownload(context: Context, animeId: Int, episode: Int) {
        if (_downloads.value[animeId]?.get(episode)?.state == STATE_PREPARING) {
            removeDownloadItem(animeId, episode)
        } else {
            val requestId = createRequestId(animeId, episode)
            DownloadService.sendRemoveDownload(
                context,
                AnimeDownloadService::class.java,
                requestId,
                false
            )
        }
    }

    fun removeAllDownloads(context: Context) {
        DownloadService.sendRemoveAllDownloads(
            context,
            AnimeDownloadService::class.java,
            false
        )
    }

    fun pauseDownload(context: Context, animeId: Int, episode: Int) {
        val requestId = createRequestId(animeId, episode)
        DownloadService.sendSetStopReason(
            context,
            AnimeDownloadService::class.java,
            requestId,
            STOP_REASON_PAUSE,
            false
        )
    }

    fun resumeDownload(context: Context, animeId: Int, episode: Int) {
        val requestId = createRequestId(animeId, episode)
        DownloadService.sendSetStopReason(
            context,
            AnimeDownloadService::class.java,
            requestId,
            Download.STOP_REASON_NONE,
            false
        )
    }

//    fun pauseAllDownloads(context: Context) {
//        DownloadService.sendPauseDownloads(
//            context,
//            AnimeDownloadService::class.java,
//            false
//        )
//    }

    fun resumeAllDownloads(context: Context) {
        DownloadService.sendResumeDownloads(
            context,
            AnimeDownloadService::class.java,
            false
        )
    }

    data class DownloadedAnime(
        val animeId: Int,
        val episode: Int,
        val state: Int,
        val stopReason: Int,
        val failureReason: Int,
        val bytesDownloaded: Long,
    )

    @UnstableApi
    private inner class TerminalStateNotificationHelper @OptIn(UnstableApi::class) constructor(
        context: Context,
        private val notificationHelper: DownloadNotificationHelper,
    ) : DownloadManager.Listener {
        private val context: Context = context.applicationContext
        private var nextNotificationId = AtomicInteger(DOWNLOADING_NOTIFICATION_ID + 1)
        private val scope = CoroutineScope(SupervisorJob())

        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?
        ) {
            when (download.state) {
                Download.STATE_COMPLETED -> {
                    val (animeId, episode) = parseRequestId(download.request.id)
                    scope.launch {
                        val anime = animeRepository.getAnimeById(animeId)
                            .firstOrNull()

                        val notification = notificationHelper.buildDownloadCompletedNotification(
                            context,
                            R.drawable.ic_download,
                            createDeepLinkPendingIntent(
                                context,
                                deepLinkToAnimePlay(context, animeId, episode)
                            ),
                            if (anime != null) "${anime.name} 第 $episode 集" else null
                        )

                        NotificationUtil.setNotification(
                            context,
                            nextNotificationId.getAndIncrement(),
                            notification
                        )
                    }
                }

                Download.STATE_FAILED -> {
                    val (animeId, episode) = parseRequestId(download.request.id)
                    scope.launch {
                        val anime = animeRepository.getAnimeById(animeId)
                            .firstOrNull()

                        val notification = notificationHelper.buildDownloadFailedNotification(
                            context,
                            R.drawable.ic_download,
                            createDeepLinkPendingIntent(
                                context,
                                deepLinkToDownloadedAnime(context, animeId)
                            ),
                            if (anime != null) "${anime.name} 第 $episode 集" else null
                        )

                        NotificationUtil.setNotification(
                            context,
                            nextNotificationId.getAndIncrement(),
                            notification
                        )
                    }
                }

                else -> return
            }

        }
    }
}

private val pendingIntentCache = mutableMapOf<Uri, PendingIntent>()

fun createDeepLinkPendingIntent(context: Context, deepLink: Uri): PendingIntent {
    return pendingIntentCache.getOrPut(deepLink) {
        TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(
                Intent(Intent.ACTION_VIEW, deepLink, context, MainActivity::class.java)
            )
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    }
}