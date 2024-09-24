package com.xioneko.android.nekoanime.ui.category

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xioneko.android.nekoanime.data.AnimeRepository
import com.xioneko.android.nekoanime.data.model.AnimeCategory
import com.xioneko.android.nekoanime.data.model.AnimeShell
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CategoryViewModel.CategoryViewModelFactory::class)
class CategoryViewModel @AssistedInject constructor(
    @Assisted initFilter: Map<AnimeCategory, Pair<String, String>>,
    private val animeRepository: AnimeRepository,
) : ViewModel() {

    @AssistedFactory
    interface CategoryViewModelFactory {
        fun create(filter: Map<AnimeCategory, Pair<String, String>>): CategoryViewModel
    }

    val animeList = mutableStateListOf<AnimeShell>()

    /**
     * Category - <OutputValue, LabelValue>
     */
    val filter = mutableStateMapOf<AnimeCategory, Pair<String, String>>()

    val fetcherState = object : FetcherState {
        override var page by mutableIntStateOf(0)
        override var hasMore by mutableStateOf(true)
        override var loadingPageCount by mutableIntStateOf(0)
    }

    init {
        initFilter.forEach { (category, pair) ->
            filter[category] = pair
        }
        fetchAnime()
    }

    fun FetcherState.reset() {
        viewModelScope.coroutineContext.cancelChildren()
        page = 0
        hasMore = true
        loadingPageCount = 0
        animeList.clear()
    }

    fun fetchAnime() {
        viewModelScope.launch {
            Log.d("Category", "Fetch Page: ${fetcherState.page + 1}")
            animeRepository.getAnimeBy(
                type = filter[AnimeCategory.Type]!!.first.toInt(),
                year = filter[AnimeCategory.Year]!!.first,
                genre = filter[AnimeCategory.Genre]!!.first,
                orderBy = filter[AnimeCategory.Order]!!.first,
                page = ++fetcherState.page
            )
                .onEach { animeShells -> animeList.addAll(animeShells) }
                .onStart { fetcherState.loadingPageCount++ }
                .onCompletion { fetcherState.loadingPageCount-- }
                .onEmpty {
                    fetcherState.hasMore = false
                    Log.d("Category", "hasMore = false")
                }
                .collect()
        }
    }
}

interface FetcherState {
    var page: Int
    var hasMore: Boolean
    var loadingPageCount: Int
}