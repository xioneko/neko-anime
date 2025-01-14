package com.xioneko.android.nekoanime.ui.search

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xioneko.android.nekoanime.data.AnimeRepository
import com.xioneko.android.nekoanime.data.UserDataRepository
import com.xioneko.android.nekoanime.data.model.AnimeShell
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
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

    private val _searchTextFlow = MutableStateFlow("")
    val searchTextFlow = _searchTextFlow.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val _candidatesFlow = _searchTextFlow
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .debounce(400)
        .flatMapLatest {
            animeRepository.getSearchSuggests(keyword = it, 10)
                .catch { Log.d("Search", "getSuggestsOf error", it) }
        }
    val candidatesFlow = _candidatesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    private val fetchingScope: CoroutineScope =
        CoroutineScope(SupervisorJob(viewModelScope.coroutineContext.job))

    val resultsViewState: ResultsViewState = ResultsViewState()

    val searchHistory = userDataRepository.searchHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    fun onInputChange(text: String) {
        _searchTextFlow.update { text }
    }

    fun addSearchRecord(text: String) {
        viewModelScope.launch {
            userDataRepository.addSearchRecord(text)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch(Dispatchers.IO) { userDataRepository.clearSearchRecord() }
    }


    fun fetchAnimeResults(keyword: String) {
        fetchingScope.coroutineContext.cancelChildren()
        resultsViewState.reset()

        fetchingScope.launch {
            resultsViewState.pageIndexFlow.collect { pageIndex ->
                Log.d("Search", "fetch $pageIndex")
                launch {
                    animeRepository.searchAnime(keyword, page = pageIndex)
                        .onStart { resultsViewState.loadingPageCount.value++ }
                        .onEach { items ->
                            val combineMap =
                                (resultsViewState.results + items).associateBy { it.id }
                            resultsViewState.results.clear()
                            resultsViewState.results.addAll(combineMap.values)
//                            resultsViewState.results.addAll(it)
                        }
                        .onCompletion { resultsViewState.loadingPageCount.value-- }
                        .onEmpty { resultsViewState.hasMore.value = false }
                        .collect()
                }
            }
        }
    }
}

data class ResultsViewState(
    val results: SnapshotStateList<AnimeShell> = mutableStateListOf(),
    val pageIndexFlow: MutableStateFlow<Int> = MutableStateFlow(0),
    var loadingPageCount: MutableState<Int> = mutableIntStateOf(0),
    var hasMore: MutableState<Boolean> = mutableStateOf(true),
) {
    fun reset() {
        results.clear()
        pageIndexFlow.update { 0 }
        loadingPageCount.value = 0
        hasMore.value = true
    }
}