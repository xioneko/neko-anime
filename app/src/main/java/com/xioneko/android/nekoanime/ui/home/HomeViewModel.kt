package com.xioneko.android.nekoanime.ui.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xioneko.android.nekoanime.data.AnimeRepository
import com.xioneko.android.nekoanime.data.model.AnimeCategory
import com.xioneko.android.nekoanime.data.model.AnimeShell
import com.xioneko.android.nekoanime.domain.GetFollowedAnimeUseCase
import com.xioneko.android.nekoanime.ui.util.LoadingState
import com.xioneko.android.nekoanime.ui.util.getRandomElements
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
        List(3) { MutableStateFlow(null to List<AnimeShell?>(FOR_YOU_ANIME_GRID_SIZE) { null }) }

    val followedAnime = getFollowedAnimeUseCase()
        .map { followed -> followed.sortedByDescending { it.lastWatchingDate } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    init {
        if (loadingState.value is LoadingState.IDLE) {
            refresh()
        }
    }

    fun refresh(onFinished: (() -> Unit)? = null) {
        with(fetchingScope) {
            _loadingState.update { LoadingState.LOADING }
            launch {
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
            .getAnimeBy(type = 1, page = 0)
            .map { it.take(RECENT_UPDATES_SIZE) }
            .catch { notifyFailure("数据源似乎出了问题", it) }
            .onEach { _recentUpdates.emit(it) }
            .collect()
    }

    private suspend fun fetchForYouAnime() = supervisorScope {
        AnimeCategory.Type.options.filter { it.second != "欧美动漫" } // 欧美动漫数量较少，暂时不显示
            .forEachIndexed { index, (type, label) ->
                launch {
                    var randomLetter = ('A'..'Z').random()
                    var randomPage = (1..5).random()
                    val forYouList = mutableListOf<AnimeShell>()
                    var errorOccurred = false
                    while (true) {
                        animeRepository
                            .getAnimeBy(
                                type = type.toInt(),
                                letter = randomLetter.toString(),
                                page = randomPage
                            )
                            .map { it.getRandomElements(FOR_YOU_ANIME_GRID_SIZE - forYouList.size) }
                            .catch {
                                notifyFailure("数据源似乎出了问题", it)
                                errorOccurred = true
                            }
                            .collect {
                                forYouList.addAll(it)
                            }
                        if (errorOccurred) break
                        else if (forYouList.size < FOR_YOU_ANIME_GRID_SIZE) {
                            randomLetter = if (randomLetter == 'Z') 'A' else randomLetter.inc()
                            randomPage = if (randomPage == 1) 1 else randomPage / 2
                        } else {
                            forYouAnimeStreams[index].emit(label to forYouList)
                            break
                        }
                    }
                }
            }
    }

    private fun notifyFailure(message: String, error: Throwable? = null) {
        Log.d("Home", message, error)
        _loadingState.update { LoadingState.FAILURE(message) }
    }
}