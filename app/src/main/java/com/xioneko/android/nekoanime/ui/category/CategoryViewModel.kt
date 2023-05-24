package com.xioneko.android.nekoanime.ui.category

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xioneko.android.nekoanime.data.AnimeRepository
import com.xioneko.android.nekoanime.data.model.AnimeShell
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val animeRepository: AnimeRepository,
) : ViewModel() {

    val animeList = mutableStateListOf<AnimeShell>()

    val filter = mutableStateMapOf(
        Category.Region to ("" to "日本"),
        Category.Type to ("" to "全部"),
        Category.Year to ("" to "全部"),
        Category.Quarter to ("" to "全部"),
        Category.Status to ("" to "全部"),
        Category.Genre to ("" to "全部"),
        Category.Order to ("" to "更新时间")
    )

    val fetcherState = object : FetcherState {
        override var page = 0
        override var hasMore by mutableStateOf(true)
        override var loadingPageCount by mutableStateOf(0)
    }

    fun FetcherState.reset() {
        viewModelScope.coroutineContext.cancelChildren()
        page = 0
        hasMore = true
        loadingPageCount = 0
        animeList.clear()
    }

    fun init(filter: Map<Category, Pair<String, String>>) {
        filter.forEach {
            this.filter.apply { set(it.key, it.value) }
        }
        fetchAnime(1)
    }

    fun fetchAnime(pageCount: Int) {
        viewModelScope.launch {
            repeat(pageCount) {
                Log.d("Category", "Fetch Page: ${fetcherState.page}")
                launch {
                    animeRepository.getAnimeBy(
                        region = filter[Category.Region]!!.first,
                        type = filter[Category.Type]!!.first,
                        year = filter[Category.Year]!!.first,
                        quarter = filter[Category.Quarter]!!.first,
                        status = filter[Category.Status]!!.first,
                        genre = filter[Category.Genre]!!.first,
                        orderBy = filter[Category.Order]!!.first,
                        pageIndex = fetcherState.page++
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
                    .join()
            }
        }
    }
}

@Stable
interface FetcherState {
    var page: Int
    var hasMore: Boolean
    var loadingPageCount: Int
}