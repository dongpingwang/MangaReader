package com.wolf2.reader.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wolf2.reader.config.AppConfig
import com.wolf2.reader.mode.db.DatabaseHelper
import com.wolf2.reader.mode.entity.ReadTime
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.navigate
import com.wolf2.reader.ui.home.Routes
import com.wolf2.reader.util.LoadResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class HistoryUiEvent {
    data object OnNavigationToHome : HistoryUiEvent()
    data class OnNavigationToDetail(val bookUuid: String) : HistoryUiEvent()
}

data class HistoryUiState(
    val readTimeResult: LoadResult<List<ReadTime>> = LoadResult.Loading,
    val allBooks: List<Book> = emptyList()
)

class HistoryViewModel : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HistoryViewModel() as T
            }
        }
    }

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            launch() {
                DatabaseHelper.readTimeDao().observeAllReadTimes().collectLatest { v ->
                    _uiState.update { it.copy(readTimeResult = LoadResult.Success(v)) }
                }
            }

            launch() {
                val allBooks = DatabaseHelper.bookDao().getAllBooks()
                _uiState.update { it.copy(allBooks = allBooks) }
            }
        }
    }

    fun onEvent(event: HistoryUiEvent) {
        when (event) {
            is HistoryUiEvent.OnNavigationToHome -> AppConfig.homeTab.update { 0 }

            is HistoryUiEvent.OnNavigationToDetail -> navigate("${Routes.BOOK_DETAIL}/${event.bookUuid}")
        }
    }
}