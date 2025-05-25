package com.wolf2.reader.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wolf2.reader.globalViewContext
import com.wolf2.reader.mode.db.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class HomeUiEvent {
    data class OnTabChange(val tab: Int) : HomeUiEvent()
    data object OnNavigationToBrowser : HomeUiEvent()
    data object OnNavigationToSearch : HomeUiEvent()
}

data class HomeUiState(
    val curTab: Int = 0,
    val readingCount: Int = 0,
    val showBadge: Boolean = true
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
            launch(Dispatchers.IO) {
                DatabaseHelper.readRecordDao().observeReadingCount().collectLatest {
                    val readCount = it
                    Timber.d("reading count from db : $readCount")
                    _uiState.update { it.copy(readingCount = readCount) }
                }
            }
        }
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnTabChange -> {
                _uiState.update { it.copy(curTab = event.tab) }
            }

            is HomeUiEvent.OnNavigationToBrowser -> {
                globalViewContext?.navController?.navigate(Routes.BROWSER_BOOK)
            }

            is HomeUiEvent.OnNavigationToSearch -> {
                globalViewContext?.navController?.navigate(Routes.SEARCH)
            }
        }
    }
}