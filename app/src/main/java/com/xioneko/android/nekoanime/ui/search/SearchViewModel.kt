package com.xioneko.android.nekoanime.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xioneko.android.nekoanime.data.AnimeRepository
import com.xioneko.android.nekoanime.data.UserDataRepository
import com.xioneko.android.nekoanime.data.model.Anime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

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


    fun searchAnime(input: String, pageIndex: Int): Flow<Pair<Anime, Flow<Boolean>>> =
        animeRepository.getAnimeByName(input, pageIndex)
            .map {
                with(it) { this to it.isFollowed() }
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