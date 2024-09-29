package com.xioneko.android.nekoanime.ui.player

import android.content.Context
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import com.xioneko.android.nekoanime.data.AnimeDownloadHelper
import com.xioneko.android.nekoanime.data.AnimeDownloadManager
import com.xioneko.android.nekoanime.data.AnimeRepository
import com.xioneko.android.nekoanime.data.UserDataRepository
import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.ui.util.LoadingState
import com.xioneko.android.nekoanime.ui.util.TrackingIterator
import com.xioneko.android.nekoanime.ui.util.getRandomElements
import com.xioneko.android.nekoanime.ui.util.setMediaVolume
import com.xioneko.android.nekoanime.ui.util.setScreenBrightness
import com.xioneko.android.nekoanime.ui.util.setScreenOrientation
import com.xioneko.android.nekoanime.ui.util.withTracking
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

private const val FOR_YOU_ANIME_GRID_SIZE = 12

@UnstableApi
@HiltViewModel(assistedFactory = AnimePlayViewModel.AnimePlayViewModelFactory::class)
class AnimePlayViewModel @OptIn(UnstableApi::class)
@AssistedInject constructor(
    @Assisted animeId: Int,
    @Assisted initEpisode: Int?,
    @ApplicationContext context: Context,
    private val animeRepository: AnimeRepository,
    private val userDataRepository: UserDataRepository,
    private val downloadHelper: AnimeDownloadHelper
) : ViewModel() {

    @AssistedFactory
    interface AnimePlayViewModelFactory {
        fun create(animeId: Int, initEpisode: Int? = null): AnimePlayViewModel
    }

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.IDLE)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    private val _dragGestureState = MutableStateFlow<DragGestureState>(DragGestureState.None)
    val dragGestureState: StateFlow<DragGestureState> = _dragGestureState.asStateFlow()

    private val _playerState = MutableStateFlow(NekoAnimePlayerState())
    val playerState = _playerState.asStateFlow()

    private val videoFetchingJob = SupervisorJob(viewModelScope.coroutineContext.job)

    private val _uiState = MutableStateFlow<AnimePlayUiState>(AnimePlayUiState.Loading)
    val uiState = _uiState.asStateFlow()

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(run {
            val cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(AnimeDownloadManager.getDownloadCache(context))
                .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                .setCacheWriteDataSinkFactory(null) // Read-only
            HlsMediaSource.Factory(cacheDataSourceFactory)
        })
        .build().apply {
            playWhenReady = true
            pauseAtEndOfMediaItems = true
        }

    var isPausedBeforeLeave: Boolean = player.playWhenReady

    val forYouAnimeStream = MutableStateFlow(List<AnimeShell?>(FOR_YOU_ANIME_GRID_SIZE) { null })

    val followedFlow = userDataRepository.followedAnimeIds
        .map { animeId in it }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            false
        )

    val downloadsState = downloadHelper.downloads
        .mapNotNull {
            when (uiState.value) {
                AnimePlayUiState.Loading -> null
                is AnimePlayUiState.Data -> it[(uiState.value as AnimePlayUiState.Data).anime.id]
                    ?.mapValues { (_, value) -> value.state }
                    ?: emptyMap()
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            null
        )

    private val orientationRequestFlow = MutableSharedFlow<Pair<Context, Int>>(replay = 1)

    val enablePortraitFullscreen = userDataRepository.enablePortraitFullscreen.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )

    var episode = mutableStateOf(initEpisode)

    private lateinit var streamIterator: TrackingIterator<Int>

    init {
        loadingUiState(animeId)

        viewModelScope.launch {
            producePlayerState()
                .collect {
                    _playerState.emit(it)
                }
        }

        viewModelScope.launch {
            orientationRequestFlow
                .conflate()
                .distinctUntilChangedBy { (_, orientation) -> orientation }
                .collect { (context, _) ->
                    delay(200) // 避免在设备方向还未就位时提前恢复自动旋转
                    context.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    Log.d("Orientation", "恢复自动旋转")
                }
        }
    }

    fun loadingUiState(animeId: Int) {
        viewModelScope.launch {
            _uiState.emit(AnimePlayUiState.Loading)

            animeRepository.getAnimeById(animeId)
                .onStart { _loadingState.emit(LoadingState.LOADING) }
                .onEmpty { notifyFailure("数据源似乎出了问题") }
                .combine(userDataRepository.watchHistory.take(1)) { anime, watchRecords ->
                    streamIterator = anime.streamIds.toSortedSet().iterator().withTracking()
                    _uiState.emit(AnimePlayUiState.Data(anime = anime))
                    if (episode.value == null) {
                        episode.value = watchRecords[animeId]?.recentEpisode ?: 1
                    }

                    launch { fetchingForYouAnime() }

                    if (!streamIterator.hasNext()) {
                        notifyFailure("找不到可用的播放地址")
                        return@combine
                    }
                    player.update(streamIterator.next())
                }
                .collect()
        }
    }

    fun unlockOrientation(context: Context, orientation: Int) {
        orientationRequestFlow.tryEmit(context to orientation)
    }

    fun onEpisodeChange(episode: Int) {
        this.episode.value = episode
        player.update(streamIterator.current!!)
    }

    private fun ExoPlayer.update(streamId: Int) {
        with(_uiState.value as AnimePlayUiState.Data) {
            clearMediaItems()
            videoFetchingJob.cancelChildren()
            Log.d("Video", "加载 ${anime.name} 节点 ${episode.value}，播放线路 $streamId")
            viewModelScope.launch(videoFetchingJob) {
                animeRepository.getVideoUrl(anime, episode.value!!, streamId)
                    .onStart { _loadingState.emit(LoadingState.LOADING) }
                    .onEmpty { tryNextStream() }
                    .collect { url ->
                        addMediaItem(MediaItem.fromUri(url))
                        Log.d("Video", "添加播放地址: $url")
                        prepare()
                        restoreWatchRecord()
                    }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }

    fun followAnime(anime: Anime) {
        viewModelScope.launch {
            userDataRepository.addFollowedAnimeId(anime.id)
        }
    }

    fun unfollowAnime(anime: Anime) {
        viewModelScope.launch {
            userDataRepository.unfollowedAnime(anime.id)
        }
    }

    fun upsertWatchRecord(episode: Int) {
        with(_uiState.value as AnimePlayUiState.Data) {
            viewModelScope.launch {
                userDataRepository.upsertWatchRecord(
                    animeId = anime.id,
                    episode = episode,
                    positionMs = player.currentPosition,
                    percentageProgress = (player.currentPosition * 100 / player.duration).toInt()
                )
            }
        }
    }

    fun restoreWatchRecord() {
        with(_uiState.value as AnimePlayUiState.Data) {
            viewModelScope.launch {
                userDataRepository.watchHistory.take(1)
                    .firstOrNull()
                    ?.get(anime.id)
                    ?.let { record ->
                        player.seekTo(record.positions[episode.value] ?: 0)
                    }
            }
        }
    }

    fun onDragGesture(context: Context, event: DragGestureEvent) {
        when (event) {
            is DragGestureEvent.Start -> {
                _dragGestureState.update {
                    DragGestureState.Data(
                        type = event.dragType,
                        value = event.startValue,
                    )
                }
            }

            is DragGestureEvent.Update -> {
                _dragGestureState.update {
                    with(it as DragGestureState.Data) {
                        when (it.type) {
                            DragType.Volume -> {
                                context.setMediaVolume(event.newValue.toFloat())
                            }

                            DragType.Brightness -> {
                                context.setScreenBrightness(event.newValue.toFloat())
                            }

                            else -> {}
                        }
                        it.copy(value = event.newValue)
                    }
                }
            }

            is DragGestureEvent.End -> {
                val endState = dragGestureState.value
                if (endState is DragGestureState.Data) {
                    if (endState.type == DragType.Progress) {
                        player.seekTo(endState.value.toLong())
                    }
                    _dragGestureState.update {
                        DragGestureState.None
                    }
                }
            }

            is DragGestureEvent.Cancel -> {
                _dragGestureState.update {
                    DragGestureState.None
                }
            }
        }
    }

    fun onOfflineCache(context: Context, episode: Int) {
        with(_uiState.value as AnimePlayUiState.Data) {
            downloadHelper.sendRequest(context, anime.id, episode)
        }
    }


    private suspend fun fetchingForYouAnime() {
        with(_uiState.value as AnimePlayUiState.Data) {
            val forYouList = mutableListOf<AnimeShell>()
            var errorOccurred = false
            for (tag in anime.tags.shuffled()) {
                animeRepository.searchAnime(tag = tag, page = (1..5).random())
                    .map { it.getRandomElements(FOR_YOU_ANIME_GRID_SIZE - forYouList.size) }
                    .catch {
                        notifyFailure("数据源似乎出了问题")
                        errorOccurred = true
                    }
                    .collect {
                        forYouList.addAll(it)
                    }
                if (forYouList.size == FOR_YOU_ANIME_GRID_SIZE || errorOccurred) break
            }
            if (errorOccurred) return
            if (forYouList.size < FOR_YOU_ANIME_GRID_SIZE) {
                animeRepository.getAnimeBy(type = 1, page = (1..30).random())
                    .map { it.getRandomElements(FOR_YOU_ANIME_GRID_SIZE - forYouList.size) }
                    .catch { notifyFailure("数据源似乎出了问题") }
                    .collect {
                        forYouList.addAll(it)
                    }
            }
            forYouAnimeStream.emit(forYouList)
        }
    }


    private fun producePlayerState() =
        callbackFlow {
            val playerListener = object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    super.onEvents(player, events)
                    val position = player.currentPosition
                    val isPaused = !player.playWhenReady
                    val isPlaying = player.isPlaying
                    val totalDurationMs = player.duration.coerceAtLeast(0L)
                    val bufferedPercentage = player.bufferedPercentage

                    val isLoading: Boolean
                    val isEnded: Boolean
                    when (player.playbackState) {
                        Player.STATE_IDLE, Player.STATE_BUFFERING -> {
                            isLoading = true
                            isEnded = false
                        }

                        Player.STATE_READY -> {
                            isLoading = false
                            isEnded = false
                        }

                        Player.STATE_ENDED -> {
                            isEnded = true
                            isLoading = player.mediaItemCount == 0
                        }

                        else -> error("Unreachable.")
                    }

                    trySend(
                        NekoAnimePlayerState(
                            isLoading,
                            isPlaying,
                            isPaused,
                            isEnded,
                            totalDurationMs,
                            position,
                            bufferedPercentage
                        )
                    )
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.d("Player", "播放出错：${error.message}")
                    tryNextStream()
                }
            }
            player.addListener(playerListener)

            awaitClose { player.removeListener(playerListener) }
        }

    private fun tryNextStream() {
        if (streamIterator.hasNext()) {
            viewModelScope.launch {
                clearVideoSourceCache(episode.value!!)
                player.update(streamIterator.next())
            }
        } else {
            notifyFailure("找不到可用的播放地址")
        }
    }

    private suspend fun clearVideoSourceCache(episode: Int) {
        with(_uiState.value as AnimePlayUiState.Data) {
            animeRepository.clearVideoSourceCache(anime, episode, streamIterator.current!!)
        }
    }

    private fun notifyFailure(message: String, error: Throwable? = null) {
        Log.d("Play", message, error)
        _loadingState.update { LoadingState.FAILURE(message) }
    }
}

@Stable
sealed interface AnimePlayUiState {
    data object Loading : AnimePlayUiState
    data class Data(val anime: Anime) : AnimePlayUiState
}

@Stable
data class NekoAnimePlayerState(
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isEnded: Boolean = false,
    val totalDurationMs: Long = 0L,
    val position: Long = 0L,
    val bufferedPercentage: Int = 0
)

sealed class DragGestureEvent {
    data class Start(val dragType: DragType, val startValue: Number) : DragGestureEvent()
    data object End : DragGestureEvent()
    data object Cancel : DragGestureEvent()
    data class Update(val newValue: Number) : DragGestureEvent()
}

sealed class DragGestureState {
    data object None : DragGestureState()
    data class Data(
        val type: DragType,
        val value: Number = 0L
    ) : DragGestureState()
}

enum class DragType {
    Volume, Brightness, Progress
}