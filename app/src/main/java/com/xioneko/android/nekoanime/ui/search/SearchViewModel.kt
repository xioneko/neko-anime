package com.xioneko.android.nekoanime.ui.search

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xioneko.android.nekoanime.data.AnimeRepository
import com.xioneko.android.nekoanime.data.UserDataRepository
import com.xioneko.android.nekoanime.data.model.Anime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {
    var searchText by mutableStateOf("")

    private val fetchingScope: CoroutineScope =
        CoroutineScope(SupervisorJob(viewModelScope.coroutineContext.job))

    val resultsViewState: ResultsViewState = ResultsViewState()

    val searchHistory = userDataRepository.searchHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    fun addSearchRecord(text: String) {
        viewModelScope.launch {
            userDataRepository.addSearchRecord(text)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch(Dispatchers.IO) { userDataRepository.clearSearchRecord() }
    }


    fun getCandidatesOf(input: String): Flow<String> =
        input.trim()
            .takeIf { it.isNotEmpty() }
            ?.let { animeRepository.getRelativeAnime(it) }
            ?: emptyFlow()


    fun fetchAnimeResults(keyword: String) {
        fetchingScope.coroutineContext.cancelChildren()
        resultsViewState.reset()

        fetchingScope.launch {
            resultsViewState.pageIndexFlow.collect { pageIndex ->
                Log.d("Search", "fetch $pageIndex")
                launch {
                    animeRepository.getAnimeByName(keyword, pageIndex)
                        .map { with(it) { this to it.isFollowed() } }
                        .onStart { resultsViewState.loadingPageCount.value++ }
                        .onEach { resultsViewState.results.add(it) }
                        .onCompletion { resultsViewState.loadingPageCount.value-- }
                        .onEmpty { resultsViewState.hasMore.value = false }
                        .collect()
                }
            }
        }
    }


    fun addFollowedAnime(anime: Anime) {
        viewModelScope.launch(Dispatchers.IO) {
            userDataRepository.addFollowedAnimeId(anime.id)
        }
    }

    fun unfollowedAnime(anime: Anime) {
        viewModelScope.launch(Dispatchers.IO) {
            userDataRepository.unfollowedAnime(anime.id)
        }
    }

    private fun Anime.isFollowed(): Flow<Boolean> = userDataRepository.isFollowed(this)
}

data class ResultsViewState(
    val results: SnapshotStateList<Pair<Anime, Flow<Boolean>>> = mutableStateListOf(),
    val pageIndexFlow: MutableStateFlow<Int> = MutableStateFlow(0),
    var loadingPageCount: MutableState<Int> = mutableStateOf(0),
    var hasMore: MutableState<Boolean> = mutableStateOf(true),
) {
    fun reset() {
        results.clear()
        pageIndexFlow.update { 0 }
        loadingPageCount.value = 0
        hasMore.value = true
    }
}