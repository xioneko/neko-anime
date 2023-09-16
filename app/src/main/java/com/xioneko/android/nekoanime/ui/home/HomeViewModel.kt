package com.xioneko.android.nekoanime.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xioneko.android.nekoanime.data.AnimeRepository
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.data.model.Category
import com.xioneko.android.nekoanime.domain.GetFollowedAnimeUseCase
import com.xioneko.android.nekoanime.ui.util.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject


const val RECENT_UPDATES_SIZE = 12
private const val FOR_YOU_ANIME_GRID_SIZE = 12

@HiltViewModel
class HomeViewModel @Inject constructor(
    getFollowedAnimeUseCase: GetFollowedAnimeUseCase,
    private val animeRepository: AnimeRepository,
) : ViewModel() {

    private val fetchingScope: CoroutineScope =
        CoroutineScope(SupervisorJob(viewModelScope.coroutineContext.job))

    var isSearching: Boolean by mutableStateOf(false)

    private val _recentUpdates = MutableStateFlow(List<AnimeShell?>(RECENT_UPDATES_SIZE) { null })
    val recentUpdates: StateFlow<List<AnimeShell?>> = _recentUpdates.asStateFlow()

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.IDLE)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    val forYouAnimeStreams: List<MutableStateFlow<Pair<String?, List<AnimeShell?>>>> =
        List(4) { MutableStateFlow(null to List<AnimeShell?>(FOR_YOU_ANIME_GRID_SIZE) { null }) }

    val followedAnime = getFollowedAnimeUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    init {
        refresh()
    }
    fun refresh(onFinished: (() -> Unit)? = null) {
        with(fetchingScope) {
            launch {
                _loadingState.emit(LoadingState.LOADING)
                joinAll(
                    launch {
                        _recentUpdates.emit(List(RECENT_UPDATES_SIZE) { null })
                        fetchRecentUpdates()
                    },
                    launch {
                        forYouAnimeStreams.forEach { it.emit(null to List(FOR_YOU_ANIME_GRID_SIZE) { null }) }
                        fetchForYouAnime()
                    }
                )
                _loadingState.emit(LoadingState.IDLE)
                onFinished?.invoke()
            }
        }
    }

    private suspend fun fetchRecentUpdates() {
        animeRepository
            .getAnimeBy(region = "日本", pageIndex = 0)
            .map { it.take(RECENT_UPDATES_SIZE) }
            .onEmpty { _loadingState.emit(LoadingState.FAILURE("😣 数据源似乎出了问题")) }
            .onEach { _recentUpdates.emit(it) }
            .collect()
    }

    private suspend fun fetchForYouAnime() = supervisorScope {
        generateForYouGenres().forEachIndexed { index, genre ->
            launch {
                animeRepository
                    .getAnimeBy(
                        region = "日本",
                        orderBy = listOf("点击量", "点击量").random(),
                        genre = genre,
                        pageIndex = (0..2).random()
                        // TODO: 不同风格的番剧数量不一，无法确定页码在有效范围内，暂时缩小范围
                    )
                    .map { it.take(FOR_YOU_ANIME_GRID_SIZE) }
                    .onEmpty { _loadingState.emit(LoadingState.FAILURE("😣 数据源似乎出了问题")) }
                    .onEach { forYouAnimeStreams[index].emit(genre to it) }
                    .collect()
            }
        }
    }

    private fun generateForYouGenres(): Set<String> = buildSet {
        val options = Category.Genre.options.drop(1).take(20)
        while (size < 4) {
            add(options.random().first)
        }
    }

}