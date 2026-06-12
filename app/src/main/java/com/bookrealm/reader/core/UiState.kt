package com.bookrealm.reader.core

/**
 * 全 App 统一的界面状态机:Loading / Success / Error。
 * ViewModel 暴露 StateFlow<UiState<T>>,Compose 按状态渲染。
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
