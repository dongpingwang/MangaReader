package com.wolf2.reader.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wolf2.reader.config.AppConfig
import com.wolf2.reader.mode.db.DatabaseHelper
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.navigate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class HomeUiEvent {
    data class OnTabChange(val tab: Int) : HomeUiEvent()
    data object OnNavigationToBrowser : HomeUiEvent()
    data object OnNavigationToSearch : HomeUiEvent()
    data class OnNavigationToRead(val book: Book) : HomeUiEvent()
    data class OnShelfLayoutModeChange(val mode: Int) : HomeUiEvent()
    data class OnShelfLayoutColumnChange(val column: Int) : HomeUiEvent()
    data object OnNavigationToSettings : HomeUiEvent()
}

data class HomeUiState(
    val curTab: Int = 0,
    val shelfLayoutMode: Int = AppConfig.shelfLayoutModeLD.value,
    val shelfLayoutColumn: Int = AppConfig.shelfLayoutModeLD.value
)

class HomeViewModel : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel() as T
            }
        }
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                AppConfig.shelfLayoutModeLD.collectLatest { v ->
                    _uiState.update { it.copy(shelfLayoutMode = v) }
                }
            }
            launch {
                AppConfig.shelfLayoutColumnLD.collectLatest { v ->
                    _uiState.update { it.copy(shelfLayoutColumn = v) }
                }

            }
        }
    }

    val latestReadBookFlow: Flow<Book?> = DatabaseHelper.bookDao().observeLatestReadBook()

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnTabChange -> {
                _uiState.update { it.copy(curTab = event.tab) }
            }

            is HomeUiEvent.OnNavigationToBrowser -> {
                navigate(Routes.BROWSER_BOOK)
            }

            is HomeUiEvent.OnNavigationToSearch -> {
                navigate(Routes.SEARCH)
            }

            is HomeUiEvent.OnNavigationToRead -> {
                navigate("${Routes.READ}/${event.book.uuid}")
            }

            is HomeUiEvent.OnShelfLayoutModeChange -> {
                AppConfig.shelfLayoutModeLD.value = event.mode
            }

            is HomeUiEvent.OnShelfLayoutColumnChange -> {
                AppConfig.shelfLayoutColumnLD.value = event.column
            }

            is HomeUiEvent.OnNavigationToSettings -> {
                navigate(Routes.SETTINGS)
            }
        }
    }
}