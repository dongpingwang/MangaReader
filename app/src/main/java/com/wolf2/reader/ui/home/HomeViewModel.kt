package com.wolf2.reader.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wolf2.reader.config.AppConfig
import com.wolf2.reader.globalViewContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class HomeUiEvent {
    data class OnTabChange(val tab: Int) : HomeUiEvent()
    data object OnNavigationToBrowser : HomeUiEvent()
    data object OnNavigationToSearch : HomeUiEvent()
    data class OnShelfLayoutModeChange(val mode: Int) : HomeUiEvent()
    data class OnShelfLayoutColumnChange(val column: Int) : HomeUiEvent()
}

data class HomeUiState(
    val curTab: Int = 0,
    val shelfLayoutMode: Int = AppConfig.shelfLayoutModeLD.value!!,
    val shelfLayoutColumn: Int = AppConfig.shelfLayoutModeLD.value!!
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

    private val shelfLayoutModeObserver = object : (Int) -> Unit {
        override fun invoke(v: Int) {
            _uiState.update { it.copy(shelfLayoutMode = v) }
        }
    }

    private val shelfLayoutColumnObserver = object : (Int) -> Unit {
        override fun invoke(v: Int) {
            _uiState.update { it.copy(shelfLayoutColumn = v) }
        }
    }

    init {
        viewModelScope.launch {
            launch(Dispatchers.Main) {
                AppConfig.shelfLayoutModeLD.observeForever(shelfLayoutModeObserver)
                AppConfig.shelfLayoutColumnLD.observeForever(shelfLayoutColumnObserver)
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

            is HomeUiEvent.OnShelfLayoutModeChange -> {
                AppConfig.shelfLayoutModeLD.postValue(event.mode)
            }

            is HomeUiEvent.OnShelfLayoutColumnChange -> {
                AppConfig.shelfLayoutColumnLD.postValue(event.column)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            launch(Dispatchers.Main) {
                AppConfig.shelfLayoutModeLD.removeObserver(shelfLayoutModeObserver)
                AppConfig.shelfLayoutColumnLD.removeObserver(shelfLayoutColumnObserver)
            }
        }
    }
}