package com.xioneko.android.nekoanime.ui.download

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import com.xioneko.android.nekoanime.data.AnimeDownloadHelper
import com.xioneko.android.nekoanime.data.AnimeRepository
import com.xioneko.android.nekoanime.data.model.Anime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class MyDownloadsViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    private val animeDownloadHelper: AnimeDownloadHelper,
    private val animeRepository: AnimeRepository,
) : ViewModel() {
    private val _downloadedAnime: MutableStateFlow<List<DownloadsItem>?> = MutableStateFlow(null)
    val downloadedAnime = _downloadedAnime.asStateFlow()

    init {
        viewModelScope.launch {
            animeDownloadHelper.downloads.map {
                it.mapNotNull { (animeId, epToDownloads) ->
                    val e2d = epToDownloads.values.filterNot { it.state == Download.STATE_REMOVING }
                    if (e2d.isEmpty()) {
                        return@mapNotNull null
                    }
                    val anime = animeRepository.getAnimeById(animeId).firstOrNull()
                    if (anime == null) {
                        Log.d("Download", "Anime fetch failed: $animeId")
                        return@mapNotNull null
                    }
                    DownloadsItem(
                        anime = anime,
                        numberOfDownloads = e2d.size
                    )
                }
            }.collect {
                _downloadedAnime.value = it
            }
        }
    }

    fun removeAllDownloads(context: Context) {
        _downloadedAnime.update { emptyList() } // 乐观更新
        animeDownloadHelper.removeAllDownloads(context)
    }
}

data class DownloadsItem(
    val anime: Anime,
    val numberOfDownloads: Int,
)