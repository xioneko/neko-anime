package com.xioneko.android.nekoanime.ui.player

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.xioneko.android.nekoanime.data.AnimeRepository
import com.xioneko.android.nekoanime.data.UserDataRepository
import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.data.model.Category
import com.xioneko.android.nekoanime.ui.util.LoadingState
import com.xioneko.android.nekoanime.ui.util.setScreenOrientation
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
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.floor

private const val FOR_YOU_ANIME_GRID_SIZE = 12

@HiltViewModel
class AnimePlayViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val animeRepository: AnimeRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.IDLE)

    private val _progressDragState = MutableStateFlow<ProgressDragState>(ProgressDragState.None)

    private val _playerState = MutableStateFlow(NekoAnimePlayerState())

    private val videoFetchingJob = SupervisorJob(viewModelScope.coroutineContext.job)

    private val _uiState = MutableStateFlow<AnimePlayUiState>(AnimePlayUiState.Loading)

    val uiState = _uiState.asStateFlow()

    @SuppressLint("UnsafeOptInUsageError")
    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        playWhenReady = true
        pauseAtEndOfMediaItems = true
    }

    val playerState = _playerState.asStateFlow()

    var isPausedBeforeLeave: Boolean = player.playWhenReady

    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    val forYouAnimeStream = MutableStateFlow(List<AnimeShell?>(FOR_YOU_ANIME_GRID_SIZE) { null })

    val progressDragState: StateFlow<ProgressDragState> = _progressDragState.asStateFlow()

    lateinit var followed: StateFlow<Boolean>

    private val orientationRequestFlow = MutableSharedFlow<Pair<Context, Int>>(replay = 1)

    val enablePortraitFullscreen = userDataRepository.enablePortraitFullscreen.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )


    init {
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
                    Log.d("Player", "恢复自动旋转")
                }
        }
    }

    fun unlockOrientation(context: Context, orientation: Int) {
        orientationRequestFlow.tryEmit(context to orientation)
    }

    fun loadingUiState(animeId: Int) {
        viewModelScope.launch {
            _uiState.emit(AnimePlayUiState.Loading)

            animeRepository.getAnimeById(animeId)
                .onStart { _loadingState.emit(LoadingState.LOADING) }
                .onEmpty { notifyFailure("🥹 数据源似乎出了问题") }
                .combine(userDataRepository.watchHistory.take(1)) { anime, watchRecords ->
                    _uiState.emit(
                        AnimePlayUiState.Data(
                            anime = anime,
                            episode = mutableIntStateOf(watchRecords[animeId]?.recentEpisode ?: 1),
                        )
                    )

                    player.update()

                    followed = userDataRepository.followedAnimeIds
                        .map { animeId in it }
                        .stateIn(
                            viewModelScope,
                            SharingStarted.WhileSubscribed(5_000),
                            true
                        )

                    fetchingForYouAnime()
                }
                .collect()
        }
    }

    fun onEpisodeChange(episode: Int) {
        with(_uiState.value as AnimePlayUiState.Data) {
            upsertWatchRecord(this.episode.value)
            this.episode.value = episode
        }
        player.update()
    }

    private fun ExoPlayer.update() {
        with(_uiState.value as AnimePlayUiState.Data) {
            clearMediaItems()
            videoFetchingJob.cancelChildren()

            Log.d("Video", "加载 ${anime.name} 第 ${episode.value} 集")
            viewModelScope.launch(videoFetchingJob) {
                animeRepository.getVideoUrl(anime, episode.value)
                    .onStart { _loadingState.emit(LoadingState.LOADING) }
                    .onEmpty {
                        notifyFailure("😣 找不到可用的播放地址")
                        Log.d("Video", "找不到可用的播放地址")
                    }
                    .collect { urls ->
                        urls.forEach {
                            addMediaItem(MediaItem.fromUri(it))
                            Log.d("Video", "添加播放地址: $it")
                        }
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

    fun onProgressDrag(event: ProgressDragEvent) {
        when (event) {
            is ProgressDragEvent.Start -> {
                _progressDragState.update {
                    ProgressDragState.Data(
                        startPosition = player.currentPosition,
                        endPosition = player.currentPosition,
                    )
                }
            }

            is ProgressDragEvent.Update -> {
                _progressDragState.update {
                    (it as ProgressDragState.Data).copy(endPosition = event.position)
                }
            }

            is ProgressDragEvent.End -> {
                player.seekTo((progressDragState.value as ProgressDragState.Data).endPosition)
                _progressDragState.update {
                    ProgressDragState.None
                }
            }

            is ProgressDragEvent.Cancel -> {
                _progressDragState.update {
                    ProgressDragState.None
                }
            }
        }
    }


    private fun fetchingForYouAnime() {
        with(_uiState.value as AnimePlayUiState.Data) {
            viewModelScope.launch {
                var pageIndex = (0..10).random()
                animeRepository
                    .getAnimeBy(
                        region = "日本",
                        genre = anime.genres.random(),
                        orderBy = Category.Order.options.random().first,
                        pageIndex = pageIndex
                    )
                    .onEmpty { error("尝试缩小 pageIndex<$pageIndex> 范围") }
                    .retry {
                        if (pageIndex == 0) false
                        else true
                            .also { pageIndex = floor(pageIndex / 2f).toInt() }
                    }
                    .catch {}
                    .firstOrNull()
                    ?.filterNot { it.id == anime.id }
                    .let {
                        if (it != null && it.size >= FOR_YOU_ANIME_GRID_SIZE) {
                            forYouAnimeStream.emit(it.subList(0, FOR_YOU_ANIME_GRID_SIZE))
                        } else {
                            val list = mutableListOf<AnimeShell>()
                            it?.let { list.addAll(it) }
                            animeRepository.getAnimeBy(region = "日本", pageIndex = 0)
                                .onEmpty { notifyFailure("😣 数据源似乎出了问题") }
                                .firstOrNull()
                                ?.let { fallbackList ->
                                    list.addAll(
                                        fallbackList.subList(0, FOR_YOU_ANIME_GRID_SIZE - list.size)
                                    )
                                    forYouAnimeStream.emit(list)
                                }
                        }
                    }


            }
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
                    super.onPlayerError(error)

                    Log.d("Video", "播放出错：${error.message}")
                    with(player) {
                        if (hasNextMediaItem()) {
                            seekToNextMediaItem()
                            prepare()
                            restoreWatchRecord()
                            Log.d("Video", "尝试加载下一个播放地址")
                            return
                        }
                    }
                    notifyFailure("😣 找不到可用的播放地址")
                }
            }
            player.addListener(playerListener)

            awaitClose { player.removeListener(playerListener) }
        }

    private fun notifyFailure(message: String) {
        _loadingState.update { LoadingState.FAILURE(message) }
    }
}

@Stable
sealed interface AnimePlayUiState {
    data object Loading : AnimePlayUiState
    class Data(
        val anime: Anime,
        var episode: MutableState<Int>,
    ) : AnimePlayUiState
}

sealed class ProgressDragEvent {
    data class Start(val bySeekBar: Boolean) : ProgressDragEvent()
    data object End : ProgressDragEvent()
    data object Cancel : ProgressDragEvent()
    data class Update(val position: Long) : ProgressDragEvent()
}

sealed class ProgressDragState {
    data object None : ProgressDragState()
    data class Data(
        val startPosition: Long = 0L,
        val endPosition: Long = 0L
    ) : ProgressDragState()
}


data class NekoAnimePlayerState(
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isEnded: Boolean = false,
    val totalDurationMs: Long = 0L,
    val position: Long = 0L,
    val bufferedPercentage: Int = 0
)