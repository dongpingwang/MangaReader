package com.wolf2.reader.ui.shelf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.wolf2.reader.globalViewContext
import com.wolf2.reader.mode.db.DatabaseHelper
import com.wolf2.reader.mode.entity.ReadRecord
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.ui.home.Routes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class BookShelfUiEvent {
    data object OnNavigationToBrowser : BookShelfUiEvent()
    data class OnItemClick(val book: Book) : BookShelfUiEvent()
    data class OnItemLongClick(val book: Book) : BookShelfUiEvent()
}

data class BookShelfUiState(
    val readRecords: List<ReadRecord> = emptyList()
)

class BookShelfViewModel : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BookShelfViewModel() as T
            }
        }
    }

    private val _uiState = MutableStateFlow(BookShelfUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                DatabaseHelper.readRecordDao().observeAll().collectLatest {
                    val readRecords = it
                    _uiState.update {
                        it.copy(readRecords = readRecords)
                    }
                }
            }
        }
    }

    val allBooksPager = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = true,
            maxSize = 300
        )
    ) {
        DatabaseHelper.bookDao().allBooks()
    }

    fun onEvent(event: BookShelfUiEvent) {
        when (event) {
            is BookShelfUiEvent.OnNavigationToBrowser -> {
                globalViewContext?.navController?.navigate(Routes.BROWSER_BOOK)
            }

            is BookShelfUiEvent.OnItemClick -> {
                globalViewContext?.navController?.navigate("${Routes.READ}/${event.book.uuid}")
            }

            is BookShelfUiEvent.OnItemLongClick -> {

            }
        }
    }
}