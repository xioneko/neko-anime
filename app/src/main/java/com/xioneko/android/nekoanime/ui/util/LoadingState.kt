package com.xioneko.android.nekoanime.ui.util

sealed interface LoadingState {
    data object IDLE : LoadingState
    data object LOADING : LoadingState
    data class FAILURE(val message: String): LoadingState
}