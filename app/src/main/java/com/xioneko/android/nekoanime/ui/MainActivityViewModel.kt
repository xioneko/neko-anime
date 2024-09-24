package com.xioneko.android.nekoanime.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xioneko.android.nekoanime.data.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    val userDataRepository: UserDataRepository,
) : ViewModel() {

    private val _isSplashScreenVisible = MutableStateFlow(true)
    val isSplashScreenVisible = _isSplashScreenVisible.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1000)
            _isSplashScreenVisible.emit(false)
        }
    }

    suspend fun isLandscapeModeDisabled() = userDataRepository.disableLandscapeMode.first()

    suspend fun shouldCheckForUpdate() = userDataRepository.updateAutoCheck.first()
}