package com.xioneko.android.nekoanime.data

import android.app.Notification
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import com.xioneko.android.nekoanime.R
import com.xioneko.android.nekoanime.ui.download.deepLinkToMyDownloads

const val DOWNLOAD_NOTIFICATION_CHANNEL = "download_channel"
const val DOWNLOADING_NOTIFICATION_ID = 8989

@UnstableApi
class AnimeDownloadService : DownloadService(
    DOWNLOADING_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_NOTIFICATION_CHANNEL,
    R.string.download_notification_channel_name,
    0,
) {
    override fun getDownloadManager(): DownloadManager = AnimeDownloadManager.getInstance(this)

    override fun getScheduler(): Scheduler? {
        return null
    }

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        return AnimeDownloadManager.getDownloadNotificationHelper(this)
            .buildProgressNotification(
                this,
                R.drawable.ic_download,
                createDeepLinkPendingIntent(this, deepLinkToMyDownloads(this)),
                "${downloadManager.currentDownloads.size} 个任务进行中",
                downloads,
                notMetRequirements,
            )
    }
}