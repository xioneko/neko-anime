package com.xioneko.android.nekoanime.ui.download

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download.STATE_REMOVING
import com.xioneko.android.nekoanime.data.AnimeDownloadHelper
import com.xioneko.android.nekoanime.data.AnimeDownloadHelper.DownloadedAnime
import com.xioneko.android.nekoanime.data.AnimeRepository
import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.data.model.asAnimeShell
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@UnstableApi
@HiltViewModel(assistedFactory = DownloadedAnimeViewModel.Factory::class)
class DownloadedAnimeViewModel @OptIn(UnstableApi::class)
@AssistedInject constructor(
    @ApplicationContext context: Context,
    @Assisted("animeId") animeId: Int,
    @Assisted("animeName") animeName: String?,
    @Assisted("imageUrl") imageUrl: String?,
    animeRepository: AnimeRepository,
    private val animeDownloadHelper: AnimeDownloadHelper,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("animeId") animeId: Int,
            @Assisted("animeName") animeName: String?,
            @Assisted("imageUrl") imageUrl: String?
        ): DownloadedAnimeViewModel
    }

    var downloadedItems = MutableStateFlow<List<DownloadedAnime>?>(null)

    val anime = animeRepository.getAnimeById(animeId)
        .map(Anime::asAnimeShell)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AnimeShell(
                id = animeId,
                name = animeName ?: "加载中...",
                imageUrl = imageUrl,
                status = ""
            )
        )

    init {
        animeDownloadHelper.resumeAllDownloads(context)

        viewModelScope.launch {
            animeDownloadHelper.downloads
                .map {
                    it[animeId]?.values
                        ?.filterNot { item -> item.state == STATE_REMOVING }
                        ?.sortedByDescending(DownloadedAnime::episode)
                        ?: emptyList()
                }
                .collect { items ->
                    downloadedItems.emit(items)
                }
        }
    }

    fun progressFlow(context: Context, episode: Int): StateFlow<Float> =
        animeDownloadHelper.downloadProgressFlow(context, anime.value.id, episode)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                0f
            )

    val selectedItems: SnapshotStateMap<Int, DownloadedAnime> = mutableStateMapOf()

    fun toggleSelection(item: DownloadedAnime) {
        if (selectedItems.containsKey(item.episode)) {
            selectedItems.remove(item.episode)
        } else {
            selectedItems[item.episode] = item
        }
    }

    fun unselectAll() = selectedItems.clear()

    fun selectAll() = downloadedItems.value?.forEach { selectedItems[it.episode] = it }

    fun removeSelected(context: Context) {
        // 乐观更新
        downloadedItems.value = downloadedItems.value
            ?.filterNot { selectedItems.containsKey(it.episode) }

        selectedItems.values.forEach { item ->
            animeDownloadHelper.removeDownload(context, item.animeId, item.episode)
        }
        selectedItems.clear()
    }

    fun pauseDownload(context: Context, item: DownloadedAnime) {
        animeDownloadHelper.pauseDownload(context, item.animeId, item.episode)
    }

    fun resumeDownload(context: Context, item: DownloadedAnime) {
        animeDownloadHelper.resumeDownload(context, item.animeId, item.episode)
    }

    fun retryDownload(context: Context, item: DownloadedAnime) {
        animeDownloadHelper.sendRequest(context, item.animeId, item.episode)
    }
}