package com.xioneko.android.nekoanime.ui.player

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.xioneko.android.nekoanime.data.AnimeRepository
import com.xioneko.android.nekoanime.data.UserDataRepository
import com.xioneko.android.nekoanime.data.model.Anime
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.data.model.Category
import com.xioneko.android.nekoanime.ui.util.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.floor

private const val FOR_YOU_ANIME_GRID_SIZE = 9

@HiltViewModel
class AnimePlayViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val animeRepository: AnimeRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.IDLE)

    private val _playerState = MutableStateFlow(NekoAnimePlayerState())

    var uiState: AnimePlayUiState by mutableStateOf(AnimePlayUiState.Loading)

    @SuppressLint("UnsafeOptInUsageError")
    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        playWhenReady = true
        pauseAtEndOfMediaItems = true
    }

    val playerState = _playerState.asStateFlow()

    var isPausedBeforeLeave: Boolean = player.playWhenReady

    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    val forYouAnimeStream = MutableStateFlow(List<AnimeShell?>(FOR_YOU_ANIME_GRID_SIZE) { null })

    lateinit var followed: StateFlow<Boolean>

    init {
        viewModelScope.launch {
            producePlayerState()
                .collect {
                    _playerState.emit(it)
                }
        }
    }

    fun loadingUiState(animeId: Int) {
        if (uiState is AnimePlayUiState.Data) return

        viewModelScope.launch {
            animeRepository.getAnimeById(animeId, skipMemory = true)
                .onStart { _loadingState.emit(LoadingState.LOADING) }
                .onEmpty { _loadingState.emit(LoadingState.FAILURE("🥹 数据源似乎出了问题")) }
                .combine(userDataRepository.watchHistory.take(1)) { anime, watchRecords ->
                    uiState = AnimePlayUiState.Data(
                        anime = anime,
                        episode = mutableStateOf(watchRecords[animeId]?.recentEpisode ?: 1)
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


    fun ExoPlayer.update() {
        with(uiState as AnimePlayUiState.Data) {
            clearMediaItems()
            viewModelScope.launch {
                fetchVideoUrl(episode.value)
                    .onStart { _loadingState.emit(LoadingState.LOADING) }
                    .onEmpty {
                        _loadingState.emit(LoadingState.FAILURE("😣 找不到可用的播放地址"))
                        Log.d("Video", "未找到可用的播放地址")
                    }
                    .combine(
                        userDataRepository.watchHistory.take(1)
                    ) { urls, watchRecords ->
                        urls.forEach { addMediaItem(MediaItem.fromUri(it)) }
                        Log.d("Video", "视频地址加载成功<$urls>")
                        prepare()
                        watchRecords[anime.id]?.positions?.get(episode.value)?.let {
                            player.seekTo(it)
                        } ?: player.seekToDefaultPosition()
                    }
                    .collect()
            }
        }
    }

    private fun AnimePlayUiState.Data.fetchVideoUrl(episode: Int) =
        animeRepository.getVideoUrl(anime, episode)

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
        with(uiState as AnimePlayUiState.Data) {
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

    private fun fetchingForYouAnime() {
        with(uiState as AnimePlayUiState.Data) {
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
                                .onEmpty { _loadingState.emit(LoadingState.FAILURE("😣 数据源似乎出了问题")) }
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
                    val currentPosition = player.currentPosition
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
                            currentPosition,
                            bufferedPercentage
                        )
                    )
                }
            }
            player.addListener(playerListener)

            awaitClose { player.removeListener(playerListener) }
        }
}

@Stable
sealed interface AnimePlayUiState {
    object Loading : AnimePlayUiState
    class Data(
        val anime: Anime,
        var episode: MutableState<Int>,
    ) : AnimePlayUiState
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