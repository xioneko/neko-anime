package com.xioneko.android.nekoanime.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xioneko.android.nekoanime.data.UserDataRepository
import com.xioneko.android.nekoanime.util.NekoAnimeUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    val updater: NekoAnimeUpdater,
) : ViewModel() {

    private val _isSplashScreenVisible = MutableStateFlow(true)
    val isSplashScreenVisible = _isSplashScreenVisible.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                val shouldAutoCheck = userDataRepository.updateAutoCheck.first()
                if (shouldAutoCheck)
                    updater.checkForUpdate()
            }
            delay(1000)
            _isSplashScreenVisible.emit(false)
        }
    }
}