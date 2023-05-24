package com.xioneko.android.nekoanime.ui.util

sealed interface LoadingState {
    object IDLE: LoadingState
    object LOADING: LoadingState
    data class FAILURE(val message: String): LoadingState
}