package com.wolf2.reader.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wolf2.reader.config.AppConfig
import com.wolf2.reader.config.ShelfFilter
import com.wolf2.reader.config.ShelfFilter.Companion.toInt
import com.wolf2.reader.config.mmkvEmit
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
    data class OnNavigationToRead(val bookUuid: String) : HomeUiEvent()
    data class OnShelfLayoutModeChange(val mode: Int) : HomeUiEvent()
    data class OnShelfLayoutColumnChange(val column: Int) : HomeUiEvent()
    data object OnNavigationToSettings : HomeUiEvent()
    data class OnFilterChange(val filter: Int) : HomeUiEvent()
    data class OnSortChange(val sort: Int) : HomeUiEvent()
}

data class HomeUiState(
    val curTab: Int = 0,
    val shelfLayoutMode: Int = AppConfig.shelfLayoutMode.value,
    val shelfLayoutColumn: Int = AppConfig.shelfLayoutMode.value,
    val navLabelShow: Int = AppConfig.navLabelShow.value,
    val curFilter: Int = AppConfig.shelfFilter.value,
    val curSort: Int = AppConfig.shelfSort.value
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
                AppConfig.shelfLayoutMode.collectLatest { v ->
                    _uiState.update { it.copy(shelfLayoutMode = v) }
                }
            }
            launch {
                AppConfig.shelfLayoutColumn.collectLatest { v ->
                    _uiState.update { it.copy(shelfLayoutColumn = v) }
                }
            }

            launch {
                AppConfig.navLabelShow.collectLatest { v ->
                    _uiState.update { it.copy(navLabelShow = v) }
                }
            }

            launch {
                AppConfig.shelfFilter.collectLatest { v ->
                    _uiState.update { it.copy(curFilter = v) }
                }
            }

            launch {
                AppConfig.shelfSort.collectLatest { v ->
                    _uiState.update { it.copy(curSort = v) }
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
                navigate("${Routes.READ}/${event.bookUuid}/${Routes.HOME}/-1")
            }

            is HomeUiEvent.OnShelfLayoutModeChange -> {
                AppConfig.shelfLayoutMode.mmkvEmit(event.mode)
            }

            is HomeUiEvent.OnShelfLayoutColumnChange -> {
                AppConfig.shelfLayoutColumn.mmkvEmit(event.column)
            }

            is HomeUiEvent.OnNavigationToSettings -> {
                navigate(Routes.SETTINGS)
            }

            is HomeUiEvent.OnFilterChange -> {
                AppConfig.shelfFilter.update { event.filter }
            }

            is HomeUiEvent.OnSortChange -> {
                AppConfig.shelfFilter.update { ShelfFilter.ALL.toInt() }
                AppConfig.shelfSort.mmkvEmit(event.sort)
            }
        }
    }
}