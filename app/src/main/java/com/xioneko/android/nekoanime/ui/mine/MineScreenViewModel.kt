package com.xioneko.android.nekoanime.ui.mine

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.xioneko.android.nekoanime.data.AnimeRepository
import com.xioneko.android.nekoanime.data.UserDataRepository
import com.xioneko.android.nekoanime.data.model.ThemeConfig
import com.xioneko.android.nekoanime.data.model.sortedByDate
import com.xioneko.android.nekoanime.domain.GetFollowedAnimeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MineScreenViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val animeRepository: AnimeRepository,
    private val userDataRepository: UserDataRepository,
    getFollowedAnimeUseCase: GetFollowedAnimeUseCase
) : ViewModel() {
    private val imageLoader = context.imageLoader

    val themeConfig = userDataRepository.themeConfig
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val followedAnime = getFollowedAnimeUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val watchHistory =
        userDataRepository.watchHistory
            .mapLatest {
                it.sortedByDate()
                    .groupBy { record ->
                        when {
                            isSameDay(record.value.date, Calendar.getInstance()) -> WatchPeriod.TODAY
                            isSameWeek(record.value.date, Calendar.getInstance()) -> WatchPeriod.THIS_WEEK
                            else -> WatchPeriod.EARLIER
                        }
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyMap()
            )

    val updateAutoCheck = userDataRepository.updateAutoCheck.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val disableLandscapeMode = userDataRepository.disableLandscapeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    fun setTheme(themeConfig: ThemeConfig) {
        viewModelScope.launch {
            userDataRepository.setThemeConfig(themeConfig)
        }
    }

    fun getAnimeById(animeId: Int) = animeRepository.getAnimeById(animeId)

    fun clearWatchRecords() {
        viewModelScope.launch {
            userDataRepository.clearWatchRecord()
        }
    }

    fun setUpdateAutoCheck(enable: Boolean) {
        viewModelScope.launch {
            userDataRepository.setUpdateAutoCheck(enable)
        }
    }

    @OptIn(ExperimentalCoilApi::class)
    fun clearAnimeCache(onFinished: suspend () -> Unit = {}) {
        viewModelScope.launch {
            animeRepository.clearCache()
            imageLoader.diskCache?.clear()
            imageLoader.memoryCache?.clear()
            onFinished()
        }
    }

    fun accessGitHubRepo(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/xioneko/neko-anime/"))
        context.startActivity(intent)
    }

    fun setDisableLandscapeMode(disable: Boolean) {
        viewModelScope.launch {
            userDataRepository.setDisableLandscapeMode(disable)
        }
    }

}

enum class WatchPeriod(val title: String) {
    TODAY("今天"),
    THIS_WEEK("本周"),
    EARLIER("更早")
}

private fun isSameDay(d1: Calendar, d2: Calendar): Boolean {
    val year1 = d1.get(Calendar.YEAR)
    val month1 = d1.get(Calendar.MONTH)
    val day1 = d1.get(Calendar.DAY_OF_MONTH)

    val year2 = d2.get(Calendar.YEAR)
    val month2 = d2.get(Calendar.MONTH)
    val day2 = d2.get(Calendar.DAY_OF_MONTH)

    return year1 == year2 && month1 == month2 && day1 == day2
}


private fun isSameWeek(d1: Calendar, d2: Calendar): Boolean {
    val year1 = d1.get(Calendar.YEAR)
    val week1 = d1.get(Calendar.WEEK_OF_YEAR)

    val year2 = d2.get(Calendar.YEAR)
    val week2 = d2.get(Calendar.WEEK_OF_YEAR)

    return year1 == year2 && week1 == week2
}