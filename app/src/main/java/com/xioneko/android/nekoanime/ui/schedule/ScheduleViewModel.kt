package com.xioneko.android.nekoanime.ui.schedule

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xioneko.android.nekoanime.data.AnimeRepository
import com.xioneko.android.nekoanime.data.UserDataRepository
import com.xioneko.android.nekoanime.ui.util.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    animeRepository: AnimeRepository,
    userDataRepository: UserDataRepository,
) : ViewModel() {

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.IDLE)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    var filterType: ScheduleFilterType by mutableStateOf(ScheduleFilterType.ALL)

    val weeklySchedule = animeRepository.getWeeklySchedule()
        .onStart { _loadingState.emit(LoadingState.LOADING) }
        .onEmpty { _loadingState.emit(LoadingState.FAILURE("数据源似乎出了问题")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    val followedAnimeIds = userDataRepository.followedAnimeIds
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed((5_000)),
            initialValue = emptySet()
        )
}

enum class ScheduleFilterType(
    val label: String
) {
    ALL("全部"),
    FOLLOWED("已追番"),
    SERIALIZING("连载中"),
}