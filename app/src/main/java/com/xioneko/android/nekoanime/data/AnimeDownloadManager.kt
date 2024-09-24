package com.xioneko.android.nekoanime.data

import android.content.Context
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import java.io.File
import java.io.IOException
import java.util.concurrent.Executor

@UnstableApi
object AnimeDownloadManager {

    private var downloadCache: Cache? = null
    private var databaseProvider: DatabaseProvider? = null
    private var downloadManager: DownloadManager? = null
    private var downloadNotificationHelper: DownloadNotificationHelper? = null

    private fun getDownloadDatabaseProvider(context: Context): DatabaseProvider {
        if (databaseProvider == null) {
            databaseProvider = StandaloneDatabaseProvider(context)
        }
        return databaseProvider as DatabaseProvider
    }

    fun getDownloadDirectory(context: Context): File =
        context.getExternalFilesDir(null) ?: context.filesDir

    fun getDownloadCache(context: Context): Cache {
        if (downloadCache == null) {
            val downloadDir = getDownloadDirectory(context)
            downloadCache =
                SimpleCache(downloadDir, NoOpCacheEvictor(), getDownloadDatabaseProvider(context))
        }
        return downloadCache as Cache
    }

    fun getInstance(context: Context): DownloadManager {
        if (downloadManager == null) {
            downloadManager = DownloadManager(
                context,
                getDownloadDatabaseProvider(context),
                getDownloadCache(context),
                DefaultHttpDataSource.Factory(),
                Executor(Runnable::run)
            )
        }
        return downloadManager as DownloadManager
    }

    fun getDownloadNotificationHelper(context: Context): DownloadNotificationHelper {
        if (downloadNotificationHelper == null) {
            downloadNotificationHelper =
                DownloadNotificationHelper(context, DOWNLOAD_NOTIFICATION_CHANNEL)
        }
        return downloadNotificationHelper as DownloadNotificationHelper
    }


    fun addListener(context: Context, listener: DownloadManager.Listener) {
        getInstance(context).addListener(listener)
    }

    fun getAllDownloads(context: Context): List<Download> = buildList {
        try {
            getInstance(context).downloadIndex.getDownloads().use { loadedDownloads ->
                while (loadedDownloads.moveToNext()) {
                    add(loadedDownloads.download)
                }
            }
        } catch (e: IOException) {
            Log.w("Download", "Failed to query downloads", e)
        }
    }

    fun getCurrentDownloads(context: Context): List<Download> =
        getInstance(context).currentDownloads
}